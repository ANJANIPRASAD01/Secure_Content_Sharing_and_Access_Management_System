# FileVault - Implementation Details & Architecture

## 📐 System Architecture

### Three-Tier Architecture

```
┌─────────────────────────────────────────┐
│     Frontend (React SPA)                │
│  - Admin Dashboard                      │
│  - User Dashboard                       │
│  - File Management UI                   │
└────────────────┬────────────────────────┘
                 │ REST API (Axios)
                 ↓
┌─────────────────────────────────────────┐
│  Backend (Spring Boot REST API)         │
│  - Controllers                          │
│  - Services                             │
│  - Security (JWT + Spring Security)     │
│  - Exception Handling                   │
└────────────────┬────────────────────────┘
                 │ JPA/Hibernate
                 ↓
┌─────────────────────────────────────────┐
│   Database (MySQL)                      │
│  - Admin                                │
│  - User                                 │
│  - File                                 │
│  - AccessControl                        │
│  - Payment                              │
│  - Category                             │
└─────────────────────────────────────────┘
```

---

## 🔄 Request Flow

### Authentication Flow

```
1. User Registration
   User Input → POST /api/auth/{role}/register
   → AuthService.register{Role}()
   → PasswordEncoder.encode()
   → Save to DB
   → Generate JWT Token
   → Return JwtResponse ✓

2. User Login
   Email + Password → POST /api/auth/{role}/login
   → AuthenticationManager.authenticate()
   → JwtProvider.generateToken()
   → Return JWT Token ✓

3. Authenticated Request
   Request + Token → /api/**
   → JwtAuthenticationFilter
   → JwtProvider.validateToken()
   → SecurityContextHolder.setAuthentication()
   → Process Request ✓
```

### File Upload Flow

```
1. Admin selects file & metadata
2. FormData → POST /api/files/upload
3. FileService.uploadFile()
   - Create unique filename
   - Save to disk: /admin_{id}/{timestamp}_{uuid}.ext
   - Create File entity
   - Save metadata to database
4. Return FileResponse with ID ✓
```

### File Access Flow

```
1. User requests file list
   → GET /api/user/{userId}/files
   → FileService.getAvailableFilesForUser()
   → Check access for each file
   → Return FileResponse[] ✓

2. User downloads file
   → GET /api/files/download/{fileId}?userId={userId}
   → FileService.getFileByIdForDownload()
   → Check access rights
   → Return file resource ✓

3. User purchases restricted file
   → POST /api/user/{userId}/payment/purchase/{fileId}
   → PaymentService.initiatePayment()
   → Process payment (simulate)
   → AccessControlService.addPurchaseAccess()
   → Return payment confirmation ✓
```

### Access Control Flow

```
1. Admin grants access to user
   → POST /api/admin/{adminId}/access/grant
   → AccessControlService.grantAccess()
   → Create AccessControl record
   → accessType = SHARED_BY_ADMIN ✓

2. User checks file access
   → AccessControlRepository.findActiveAccess()
   → Check isActive = true
   → Return access status ✓

3. Admin revokes access
   → POST /api/admin/{adminId}/access/revoke
   → Set isActive = false
   → User can no longer access file ✓
```

---

## 🏗️ Data Models

### Entity Relationships

```
Admin (1) ────────┐
                  │ (1:M) Creates
                  ├──→ File
                  │
                  └──→ Payment (receives)

User (1) ─────────┐
                  │ (1:M)
                  ├──→ AccessControl
                  │
                  └──→ Payment (makes)

File (1) ─────────┐
                  │ (1:M)
                  ├──→ AccessControl
                  │
                  └──→ Payment

Category (1) ─────┐
                  │ (1:M)
                  └──→ File
```

### File Access Types

```
PUBLIC      → Anyone can access and download
PRIVATE     → Only admin can access
RESTRICTED  → Admin can:
              1. Share with specific users (SHARED_BY_ADMIN)
              2. Charge for access (PURCHASED)
```

---

## 🔒 Security Implementation

### Authentication

```java
// 1. Password Encryption
BCryptPasswordEncoder.encode() // Hashing passwords

// 2. JWT Token Generation
JwtProvider.generateToken(authentication)
// Claims: username, issuedAt, expiration
// Signed with HS512 algorithm

// 3. Token Validation
JwtAuthenticationFilter
// Extracts token from Authorization header
// Validates token signature and expiration
// Sets authentication context

// 4. Role-Based Access Control
SecurityConfig
// @PreAuthorize on methods
// RequestMatchers in HTTPSecurity
```

### Implementation Details

```java
// Spring Security Configuration
HttpSecurity
  .authorizeHttpRequests(authz ->
    authz.requestMatchers("/api/admin/**").hasRole("ADMIN")
         .requestMatchers("/api/user/**").hasRole("USER")
         .requestMatchers("/api/auth/**").permitAll()
  )
  .addFilterBefore(jwtAuthenticationFilter, 
                   UsernamePasswordAuthenticationFilter.class)
```

---

## 💳 Payment System

### Payment Workflow

```
1. User initiates purchase
   ↓
2. Create Payment record (PENDING)
   ↓
3. Process payment (gateway integration point)
   ↓
4. If successful:
   - Update Payment status to COMPLETED
   - Create AccessControl record (PURCHASED)
   - User can now access file
   ↓
5. If failed:
   - Update Payment status to FAILED
   - Return error message
```

### Payment Status Transitions

```
Initial: PENDING
Success: PENDING → COMPLETED → User gains AccessControl
Failure: PENDING → FAILED → No AccessControl
Refund: COMPLETED → REFUNDED
```

---

## 📁 File Storage Strategy

### Directory Structure

```
D:/filevault-uploads/
├── admin_1/
│   ├── 1708760000000_a1b2c3d4.pdf
│   ├── 1708760001000_e5f6g7h8.docx
│   └── 1708760002000_i9j0k1l2.txt
├── admin_2/
│   ├── 1708760010000_m3n4o5p6.pdf
│   └── 1708760020000_q7r8s9t0.zip
└── admin_3/
    └── 1708760030000_u1v2w3x4.mp4
```

### Filename Generation

```java
String generateUniqueFileName(String originalFileName) {
    long timestamp = System.currentTimeMillis();
    String extension = originalFileName.substring(lastIndexOf("."));
    return timestamp + "_" + UUID.randomUUID().substring(0, 8) + extension;
}
// Result: 1708760000000_a1b2c3d4.pdf
```

---

## Database Schema Highlights

### Key Indexes

```sql
-- Performance optimization
CREATE INDEX idx_email ON admins(email);
CREATE INDEX idx_email ON users(email);
CREATE INDEX idx_admin_id ON files(admin_id);
CREATE INDEX idx_access_type ON files(access_type);
CREATE INDEX idx_file_id ON access_control(file_id);
CREATE INDEX idx_user_id ON access_control(user_id);
```

### Unique Constraints

```sql
-- Prevent duplicates
UNIQUE KEY unique_file_user ON access_control(file_id, user_id);
```

---

## 🔄 Request/Response Examples

### Admin Upload File

**Request:**
```
POST /api/files/upload
Authorization: Bearer eyJhbGc...
Content-Type: multipart/form-data

file: [binary file data]
categoryId: 1
accessType: RESTRICTED
price: 9.99
description: Premium Content
```

**Response:**
```json
{
  "message": "File uploaded successfully",
  "fileId": 42,
  "fileName": "1708760000000_a1b2c3d4.pdf",
  "fileSize": 2048576
}
```

### User Purchase File

**Request:**
```
POST /api/user/123/payment/purchase/42
Authorization: Bearer eyJhbGc...
Content-Type: application/json

{
  "fileId": 42,
  "amount": 9.99,
  "paymentMethod": "CREDIT_CARD",
  "transactionId": "txn_123456"
}
```

**Response:**
```json
{
  "message": "File purchased successfully",
  "payment": {
    "id": 1,
    "status": "COMPLETED",
    "amount": 9.99,
    "completedAt": "2024-02-24T15:30:00"
  }
}
```

---

## Performance Considerations

### Database Optimization
- Indexed columns for fast queries
- Foreign key constraints
- Efficient queries with JPA

### Caching Strategy
```java
// Recommended caching:
@Cacheable("categories") // Cache categories list
public List<Category> getAllCategories() { }

@CacheEvict("categories") // Invalidate on update
public Category updateCategory() { }
```

### File Upload Optimization
- Chunk upload for large files
- Compression before storage
- Async processing

---

## Error Handling

### Exception Hierarchy

```
RuntimeException
├── ResourceNotFoundException
├── UnauthorizedAccessException
└── ValidationException

GlobalExceptionHandler catches all and returns:
{
  "error": "Error Type",
  "message": "Detailed message",
  "status": "HTTP Status Code"
}
```

### HTTP Status Codes

```
200 OK              - Successful request
201 CREATED         - Resource created
400 BAD REQUEST     - Invalid input
401 UNAUTHORIZED    - Authentication failed
403 FORBIDDEN       - Access denied
404 NOT FOUND       - Resource not found
500 SERVER ERROR    - Unexpected error
```

---

## Deployment Architecture

### Production Setup

```
┌─────────────────┐
│  Load Balancer  │
└────────┬────────┘
         │
    ┌────┴────┐
    ↓         ↓
  App1      App2      (Spring Boot instances)
    └────┬────┘
         │
    ┌────┴────┐
    ↓         ↓
  MySQL    Redis      (Database & Cache)
    └─────┬──┘
          │
    ┌─────┴─────┐
    ↓           ↓
  Backup      Monitoring
```

---

## Development Best Practices

### Code Structure
- Keep services focused (Single Responsibility)
- Use DTOs for API communication
- Implement proper exception handling
- Add logging for debugging

### Testing Strategy
- Unit tests for services
- Integration tests for repositories
- API tests with Postman
- Load testing before production

### Security Checklist
- ✅ Change JWT secret in production
- ✅ Use HTTPS in production
- ✅ Validate all inputs
- ✅ Implement rate limiting
- ✅ Regular security audits

---

## Future Enhancements

1. **Payment Integration**
   - Stripe API
   - PayPal integration
   - Wallet system

2. **Advanced Features**
   - File versioning
   - Collaboration features
   - Comments/ratings

3. **Performance**
   - Elasticsearch for file search
   - Redis caching
   - CDN for file delivery

4. **Analytics**
   - User behavior tracking
   - File popularity metrics
   - Revenue analytics

---

## Monitoring & Maintenance

### Key Metrics to Monitor
- API response times
- Database query performance
- File upload/download success rates
- Active user sessions
- Payment success rates

### Maintenance Tasks
- Regular database backups
- Log rotation
- Dependency updates
- Security patches
- Performance optimization

---

This implementation provides a production-ready foundation with room for enhancements based on specific requirements.
