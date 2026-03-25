# FileVault Codebase - Detailed Issue Analysis

## Issue 1: Admin Dashboard File Count Showing Zero

### Problem Statement
Files appear in the admin dashboard file list but `totalFilesUploaded` shows 0 in the stats card.

### Current Implementation Flow

#### Frontend (AdminDashboard.jsx:20-28)
```javascript
const fetchDashboardData = async () => {
  const [dashRes, filesRes] = await Promise.all([
    api.get(`/admin/${adminId}/dashboard`),
    api.get(`/admin/${adminId}/files`),
  ]);
  
  setDashboard(dashRes.data);
  setFiles(filesRes.data);
};
```

**Display Code (Line 87):**
```javascript
<p className="text-3xl font-bold text-green-600 mt-2">
  {dashboard.totalFilesUploaded || 0}
</p>
```

#### Backend Dashboard Endpoint (AdminController.java:89-102)

```java
@GetMapping("/{adminId}/dashboard")
public ResponseEntity<?> getAdminDashboard(@PathVariable Long adminId) {
    Map<String, Object> dashboard = new HashMap<>();
    dashboard.put("totalEarnings", adminService.getTotalEarnings(adminId));
    dashboard.put("totalFilesUploaded", adminService.getTotalFilesUploaded(adminId));
    dashboard.put("adminInfo", adminService.getAdminById(adminId));
    return new ResponseEntity<>(dashboard, HttpStatus.OK);
}
```

#### Service Implementation (AdminService.java:64-66)
```java
public Long getTotalFilesUploaded(Long adminId) {
    return (long) fileRepository.findByAdminId(adminId).size();
}
```

#### Repository Query (FileRepository.java)
```java
List<File> findByAdminId(Long adminId);
```

#### File Entity Relationship (File.java:38-41)
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "admin_id", nullable = false)
private Admin admin;
```

### Root Cause Analysis

The file count implementation appears correct on the surface:
1. Dashboard endpoint calls the service method
2. Service uses Spring Data JPA `findByAdminId()`
3. File entity correctly maps admin_id column

**Possible Issues:**
1. **Admin ID mismatch**: The `adminId` passed to controller might not match the admin_id stored in File records
2. **Lazy loading**: Using `@ManyToOne(fetch = FetchType.LAZY)` means the admin relationship isn't loaded during query
3. **Spring Data limitation**: `findByAdminId()` should work BUT depends on proper relationship mapping
4. **Actual vs. Expected behavior**: 
   - `/admin/{adminId}/files` endpoint returns files (shows count correctly)
   - `/admin/{adminId}/dashboard` returns 0 (query issue)

### Key Difference Between Working and Non-Working Endpoints

Both use same data source but different paths:
- **Working** (AdminService.java:54-57): 
  ```java
  public List<FileResponse> getAdminFiles(Long adminId) {
      List<File> files = fileRepository.findByAdminId(adminId);
      return files.stream().map(fileService::convertToFileResponse)...
  }
  ```

- **Not Working** (AdminService.java:64-66):
  ```java
  public Long getTotalFilesUploaded(Long adminId) {
      return (long) fileRepository.findByAdminId(adminId).size();
  }
  ```

Both use the same query, so the issue might be elsewhere.

---

## Issue 2: Access Request Granting Functionality

### Current Implementation

#### Frontend - RequestManagement.jsx

**Current UI Capabilities:**
- Displays pending access requests in a table
- Shows: File Name, User, Email, Status, Requested Date
- Actions: Approve, Reject buttons
- No search, no file selection, no time limit configuration

**Approve Flow (Line 25-37):**
```javascript
const handleApprove = async (requestId) => {
  const response = await axios.post(
    'http://localhost:8080/api/access-requests/approve',
    { requestId, action: 'APPROVED' },
    { headers: { Authorization: `Bearer ${token}` }, params: { adminId } }
  );
  // Removes from list
};
```

**Limitation**: Admin can ONLY approve existing requests - cannot:
1. Search for specific users
2. Select which files to grant access to
3. Set time limits for access
4. Proactively grant access without a request

#### Backend - Access Request Endpoints

**AccessRequestController.java:**

1. **Approve Request** (POST `/api/access-requests/approve`)
   ```java
   public ResponseEntity<?> approveRequest(
       @RequestBody AccessRequestActionDTO action,
       @RequestParam(value = "adminId") Long adminId)
   ```
   - Only approves pending requests
   - No file selection parameter
   - No time limit parameter

2. **Available Endpoints:**
   - `POST /request/{fileId}/{userId}` - Request access (user requests)
   - `POST /approve` - Approve pending request
   - `POST /reject` - Reject pending request
   - `GET /admin/{adminId}/pending` - Get pending requests
   - `GET /file/{fileId}` - Get requests for specific file

**Missing Endpoints:**
   - No admin-initiated access grant with file selection
   - No user search endpoint
   - No time limit setting endpoint

### Service Implementation - AccessRequestService.java

**Approve Request Flow (Line 55-80):**
```java
public AccessRequestDTO approveRequest(Long requestId, Long adminId) {
    request.setStatus(RequestStatus.APPROVED);
    request.setRespondedAt(LocalDateTime.now());
    
    // Grant access
    AccessControl access = AccessControl.builder()
        .file(request.getFile())
        .user(request.getUser())
        .accessType(AccessType.SHARED_BY_ADMIN)
        .build();
    accessControlRepository.save(access);
}
```

**Critical Finding**: The `expiresAt` field is NOT being set!

### AccessControl Entity - Time Limit Support

**AccessControl.java shows time-limit structure exists:**
```java
@Column(nullable = true)
private LocalDateTime expiresAt; // For time-limited access
```

But it's never populated when granting access (always null).

### Missing Features Summary

| Feature | Frontend | Backend | Status |
|---------|----------|---------|--------|
| User Search | ❌ None | ❌ No endpoint | Not Implemented |
| File Selection | ❌ None | ❌ No parameter | Not Implemented |
| Time Limit Config | ❌ None | ⚠️ Entity support only | Partially Implemented |
| Direct Access Grant | ❌ None | ❌ No endpoint | Not Implemented |
| Current Flow | ✅ Request/Approve only | ✅ Request/Approve only | Functional but Limited |

---

## Issue 3: Login/Navigation - Options Still Showing After Login

### Problem Statement
Login options (Admin/User login/register) persist in navigation even after user is logged in.

### Current Authentication Architecture

#### Frontend - App.jsx (Lines 1-20)

```javascript
function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [userRole, setUserRole] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem('token');
    const user = localStorage.getItem('user');
    
    if (token && user) {
      setIsAuthenticated(true);
      const userData = JSON.parse(user);
      setUserRole(userData.role);
    }
    
    setLoading(false);
  }, []);
```

**Flow:**
1. On app load, checks localStorage for token & user
2. Sets local state `isAuthenticated` and `userRole`
3. Used for route protection (protected routes check this state)

#### HomePage.jsx - Navigation Logic (Lines 50-80)

```javascript
<nav className="bg-white shadow-lg sticky top-0 z-50">
  <div className="flex justify-between items-center">
    <h1 className="text-2xl font-bold text-blue-600">FileVault</h1>
    <div className="flex items-center space-x-4">
      {localStorage.getItem('user') ? (
        // LOGGED IN: Show Dashboard & Logout
        <>
          <span className="text-gray-600">
            {JSON.parse(localStorage.getItem('user')).firstName}
          </span>
          <button 
            onClick={() => {
              const userData = JSON.parse(localStorage.getItem('user'));
              navigate(userData.role === 'ADMIN' ? '/admin/dashboard' : '/user/dashboard');
            }}
          >
            Dashboard
          </button>
          <button onClick={() => {
            localStorage.removeItem('token');
            localStorage.removeItem('user');
            window.location.reload();
          }}>
            Logout
          </button>
        </>
      ) : (
        // NOT LOGGED IN: Show login options
        <div>
          <div>
            <p>Admin?</p>
            <Link to="/admin/login">Login</Link>
            <Link to="/admin/register">Register</Link>
          </div>
          <div>
            <p>User?</p>
            <Link to="/user/login">Login</Link>
            <Link to="/user/register">Register</Link>
          </div>
        </div>
      )}
    </div>
  </div>
</nav>
```

**Navigation Logic:**
- Checks `localStorage.getItem('user')` directly
- If present → shows Dashboard & Logout
- If absent → shows Admin & User login/register links
- Logic is CORRECT at the HomePage level

### Why Login Options Might Still Show

#### Root Cause 1: Multi-Tab Synchronization Issue

**Scenario:**
1. Tab A: User logs in → localStorage updated → HomePage checks and hides login buttons ✓
2. Tab B: Still has old state → localStorage not checked → still shows login buttons ❌
3. User switches to Tab B → sees login options despite being logged in elsewhere

**Why it happens:**
- React doesn't automatically listen to localStorage changes from other tabs
- Only the tab that performed login updates its state
- Other tabs keep stale local state

#### Root Cause 2: State vs. Storage Mismatch

**Code Analysis:**
```javascript
// App.jsx - initializes state once on mount
useEffect(() => {
  const token = localStorage.getItem('token');
  const user = localStorage.getItem('user');
  if (token && user) {
    setIsAuthenticated(true);
  }
  setLoading(false);
}, []); // Empty dependency - runs only once!
```

**Problem:**
- State is set once on component mount (empty dependency array)
- If localStorage changes after mount, state isn't updated
- HomePage checks localStorage directly (works), but other components rely on stale App state

#### Root Cause 3: Incomplete Logout Flow

**Current Logout (HomePage.jsx):**
```javascript
<button onClick={() => {
  localStorage.removeItem('token');
  localStorage.removeItem('user');
  window.location.reload();  // Forces full page reload
}}>
  Logout
</button>
```

**Frontend Logout (AdminDashboard.jsx):**
```javascript
const handleLogout = () => {
  onLogout();
  navigate('/');
};
```

**Issue:**
- AdminDashboard calls `onLogout()` from App
- But App's `onLogout()` updates state
- State change doesn't trigger HomePage re-render if already on HomePage
- Navigate to '/' loads HomePage, which checks localStorage (so it works)

#### Root Cause 4: No Axios Response Interception Re-render

**Axios Config (axiosConfig.js:25-30):**
```javascript
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/';  // Force redirect, but...
    }
    return Promise.reject(error);
  }
);
```

**Issue:**
- On 401, redirects to '/'
- But if user is already on HomePage, it might not re-render
- Navigation state might show stale auth info briefly

### Authentication State Management Issues Summary

| Issue | Component | Root Cause | Impact |
|-------|-----------|-----------|--------|
| **Multi-tab sync** | HomePage | No storage event listener | Different tabs show different state |
| **Stale state** | App.jsx | UseEffect runs only once | App state doesn't update if localStorage changes |
| **Navigation timing** | Multiple | State vs localStorage async update | Brief moments showing old UI state |
| **Logout flow** | AdminDashboard → App | State update doesn't trigger HomePage | May need manual navigation |
| **401 response** | axiosConfig.js | Force redirect but no state update | UI might be stale after token expiry |

### Design Flaw: Multiple Sources of Truth

Currently:
1. **localStorage** - source of truth for persistence
2. **App.jsx state** - local state set once on mount
3. **HomePage checks localStorage directly** - works but inconsistent
4. **Protected routes check App state** - might be stale

**Better approach:** Single source of truth with localStorage listeners or context API.

---

## Summary of Findings

### Issue 1: File Count
- **Status**: Likely configuration/data issue
- **Both endpoints use same query** but dashboard shows 0
- **Possible causes**: 
  - Admin ID mismatch in database
  - Query execution timing issue
  - Database state inconsistency

### Issue 2: Access Request Features
- **Status**: Features not implemented
- **Current**: Reactive (approve/reject existing requests)
- **Missing**: 
  - User search functionality (no backend endpoint)
  - Direct file access grant (no endpoint)
  - Time limit configuration (entity support exists, but logic missing)

### Issue 3: Login/Navigation Persistence
- **Status**: Multiple concurrent issues
- **Root causes**:
  - Multi-tab state synchronization missing
  - App state initialized only once
  - HomePage checks localStorage directly (works) but inconsistent
  - No global auth context
  - Axios 401 redirects without ensuring state update
