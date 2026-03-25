# Local Development Setup - PostgreSQL

## **Prerequisites**

Install PostgreSQL on your machine:
- **Windows:** https://www.postgresql.org/download/windows/
- **Mac:** `brew install postgresql@15`
- **Linux:** Follow PostgreSQL official docs

---

## **Step 1: Create Local PostgreSQL Database**

### **Windows (CMD or PowerShell)**

```bash
# Start PostgreSQL service (if not running)
# On Windows, PostgreSQL usually starts automatically

# Open PostgreSQL Command Line (psql)
psql -U postgres

# In psql, create database:
CREATE DATABASE filevault_db;

# Create user (optional, for better security):
CREATE USER filevault_user WITH PASSWORD 'filevault_password';
ALTER USER filevault_user CREATEDB;
GRANT ALL PRIVILEGES ON DATABASE filevault_db TO filevault_user;

# Exit psql
\q
```

### **Mac/Linux**

```bash
# Start PostgreSQL
brew services start postgresql@15

# Open psql
psql postgres

# Create database:
CREATE DATABASE filevault_db;
CREATE USER filevault_user WITH PASSWORD 'filevault_password';
GRANT ALL PRIVILEGES ON DATABASE filevault_db TO filevault_user;

# Exit
\q
```

---

## **Step 2: Configure Local Backend**

Create `.env` file in `backend/` directory:

```properties
PORT=8080
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/filevault_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
JWT_SECRET=your-secret-key-for-local-testing-at-least-32-chars
JWT_EXPIRATION=86400000
CORS_ORIGINS=http://localhost:3000
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_COM_FILEVAULT=DEBUG
SPRING_JPA_HIBERNATE_DDL_AUTO=create-drop
FILE_UPLOAD_DIR=uploads
```

**Or set as environment variables:**

```bash
set SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/filevault_db
set SPRING_DATASOURCE_USERNAME=postgres
set SPRING_DATASOURCE_PASSWORD=postgres
set JWT_SECRET=your-secret-key-for-local-testing-at-least-32-chars
```

---

## **Step 3: Build Backend**

```bash
cd backend
mvn clean install
```

Should complete without errors ✅

---

## **Step 4: Run Backend Locally**

```bash
mvn spring-boot:run
```

**Expected logs:**
```
Tomcat started on port(s): 8080
Started FileVaultApplication in X seconds
HikariPool-1 - Successfully connected to database
```

---

## **Step 5: Configure & Run Frontend**

```bash
cd frontend

# Install dependencies
npm install

# Set environment variable (Windows CMD)
set REACT_APP_API_URL=http://localhost:8080/api

# Or (Mac/Linux)
export REACT_APP_API_URL=http://localhost:8080/api

# Start frontend
npm start
```

Frontend will open on `http://localhost:3000`

---

## **Step 6: Test the Application**

### **Backend Health Check**
```bash
curl http://localhost:8080/api/health
```

Should return: `200 OK`

### **Frontend Tests**
1. Visit http://localhost:3000
2. Register a new user
3. Login
4. Upload a file
5. Test access requests

---

## **Troubleshooting**

### **PostgreSQL Connection Fails**
```
java.io.IOException: connect timed out
```

**Solution:**
- Make sure PostgreSQL is running
- Check database name is `filevault_db`
- Verify username/password

### **Hibernate Schema Not Creating**
```
ERROR: relation "user" does not exist
```

**Solution:**
- Set `SPRING_JPA_HIBERNATE_DDL_AUTO=create-drop` (for development)
- If still fails, manually create schema:

```sql
psql -U postgres -d filevault_db -f database/schema.sql
```

### **Frontend Can't Connect to Backend**
- Check `REACT_APP_API_URL` matches backend URL
- Clear browser cache and restart frontend
- Check CORS is enabled for `http://localhost:3000`

---

## **Local vs Production Environment Variables**

| Variable | Local | Production |
|----------|-------|-----------|
| `PORT` | 8080 | 8080 |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/filevault_db` | Railway/Render PostgreSQL URL |
| `SPRING_DATASOURCE_USERNAME` | postgres | Production user |
| `SPRING_DATASOURCE_PASSWORD` | postgres | Production password |
| `JWT_SECRET` | any-string-for-testing | Strong random 32+ chars |
| `CORS_ORIGINS` | `http://localhost:3000` | Your Vercel frontend URL |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | create-drop or update | validate |

---

## **Quick Start Summary**

```bash
# Terminal 1: PostgreSQL
psql -U postgres
CREATE DATABASE filevault_db;
\q

# Terminal 2: Backend
cd backend
mvn spring-boot:run

# Terminal 3: Frontend
cd frontend
npm install
npm start

# Now visit http://localhost:3000
```

✅ All running locally!

---

## **Next: Deploy to Render**

Once local testing is complete:
1. Push to GitHub
2. Create PostgreSQL database on Railway or Render
3. Deploy backend to Render
4. Deploy frontend to Vercel
