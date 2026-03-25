# FileVault - Complete Setup Guide

## ⚡ Quick Start

### Backend Setup (5 minutes)

1. **Database Setup**
```bash
# Open MySQL and execute:
mysql -u root -p < database/schema.sql
```

2. **Update Configuration**
```bash
# Edit: backend/src/main/resources/application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/filevault_db
spring.datasource.username=root
spring.datasource.password=your_db_password

jwt.secret=your-very-long-secret-key-with-minimum-32-characters
jwt.expiration=86400000

file.upload-dir=D:/filevault-uploads
```

3. **Build & Run Backend**
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

Backend runs on: `http://localhost:8080`

### Frontend Setup (5 minutes)

1. **Install Dependencies**
```bash
cd frontend
npm install
```

2. **Create .env file**
```bash
REACT_APP_API_URL=http://localhost:8080/api
```

3. **Run Frontend**
```bash
npm start
```

Frontend runs on: `http://localhost:3000`

---

## 📋 Detailed Setup Instructions

### Prerequisites
- Java 17 or higher
- MySQL 8.0 or higher
- Node.js 14+ 
- npm or yarn
- Maven 3.6+

### Backend Setup

#### 1. Database Configuration

**Step 1: Create Database**
```sql
-- Open MySQL Command Line
mysql -u root -p

-- Execute SQL
USE mysql;
CREATE DATABASE IF NOT EXISTS filevault_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Verify
SHOW DATABASES;
```

**Step 2: Run Schema**
```bash
mysql -u root -p filevault_db < database/schema.sql
```

#### 2. Application Configuration

**File: `backend/src/main/resources/application.properties`**

```properties
# Application Name
spring.application.name=FileVault
server.port=8080

# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/filevault_db
spring.datasource.username=root
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA/Hibernate
spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# JWT Configuration (CHANGE IN PRODUCTION!)
jwt.secret=super-secret-key-change-this-in-production-with-random-string
jwt.expiration=86400000

# File Upload
file.upload-dir=D:/filevault-uploads
spring.servlet.multipart.max-file-size=100MB
spring.servlet.multipart.max-request-size=100MB

# Logging
logging.level.root=INFO
logging.level.com.filevault=DEBUG
```

#### 3. Build Backend

```bash
cd backend

# Clean and install dependencies
mvn clean install

# Build the project
mvn clean build

# Run the application
mvn spring-boot:run
```

**Verify Backend is Running:**
- Open browser: `http://localhost:8080/api/public`
- Should return response (or 403 Forbidden is OK)

### Frontend Setup

#### 1. Install Dependencies

```bash
cd frontend
npm install
```

#### 2. Environment Configuration

**Create `.env` file in `frontend` folder:**
```
REACT_APP_API_URL=http://localhost:8080/api
```

#### 3. Start Development Server

```bash
npm start
```

- Frontend will open automatically at `http://localhost:3000`

---

## 🧪 Testing the Application

### Admin Flow

1. **Register Admin**
   - Go to: `http://localhost:3000/admin/register`
   - Fill in: Email, Password, First Name, Last Name
   - Click Register

2. **Login Admin**
   - Go to: `http://localhost:3000/admin/login`
   - Use credentials to login
   - Should see Admin Dashboard

3. **Upload File (Backend)**
```bash
curl -X POST http://localhost:8080/api/files/upload \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@/path/to/file.pdf" \
  -F "categoryId=1" \
  -F "accessType=RESTRICTED" \
  -F "price=9.99" \
  -F "description=Premium PDF"
```

### User Flow

1. **Register User**
   - Go to: `http://localhost:3000/user/register`
   - Fill in form
   - Click Register

2. **Login User**
   - Go to: `http://localhost:3000/user/login`
   - Use credentials to login
   - Should see User Dashboard with available files

3. **Purchase File**
   - Click "Purchase Access" on restricted file
   - Confirm purchase
   - After purchase, "Download" button appears

---

## 📂 File Upload Directory

Create the upload directory manually:
```bash
# Windows
mkdir D:\filevault-uploads

# Linux/Mac
mkdir -p /usr/local/filevault-uploads
```

Update path in `application.properties`:
```properties
file.upload-dir=D:/filevault-uploads
```

---

## 🔐 Security Configuration

### JWT Secret (IMPORTANT!)

Generate a secure key:
```bash
# On Linux/Mac:
openssl rand -base64 32

# On Windows PowerShell:
[Convert]::ToBase64String([System.Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
```

Update `application.properties`:
```properties
jwt.secret=your-generated-random-key-here
```

### CORS Configuration

Edit `backend/src/main/java/com/filevault/config/CorsConfig.java`:

**For Production:**
```java
registry.addMapping("/api/**")
    .allowedOrigins("https://yourdomain.com")
    .allowedMethods("GET", "POST", "PUT", "DELETE")
    .allowedHeaders("*")
    .allowCredentials(true)
    .maxAge(3600);
```

---

## 🚀 Deployment

### Docker Setup (Optional)

**Backend Dockerfile:**
```dockerfile
FROM openjdk:17-jdk-slim
COPY target/filevault.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Build Docker Image:**
```bash
cd backend
mvn clean package
docker build -t filevault:latest .
docker run -p 8080:8080 -e SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/filevault_db filevault:latest
```

### Azure Deployment

See Azure deployment guide for App Service, Container Instances, or Kubernetes deployment.

---

## Troubleshooting

### Issue: Failed to connect to database

**Solution:**
```bash
# Check MySQL is running
mysql -u root -p

# Test connection in app:
# Add -Dspring.jpa.show-sql=true to see SQL logs
```

### Issue: JWT Token Expired

**Solution:**
- Update token expiration time in `application.properties`
- Default: 24 hours (86400000 ms)

### Issue: File upload fails

**Solution:**
```bash
# Check upload directory permissions
chmod 777 /path/to/filevault-uploads

# Check file size limits in application.properties
spring.servlet.multipart.max-file-size=100MB
```

### Issue: CORS errors in frontend

**Solution:**
- Check `CorsConfig.java` 
- Ensure frontend URL is in allowed origins
- Clear browser cache and cookies

---

## API Testing with Postman

### Import Collection

1. Create new Postman Collection
2. Add requests as shown below

### Register Admin
```
POST http://localhost:8080/api/auth/admin/register
Content-Type: application/json

{
  "email": "admin@test.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+1234567890"
}
```

### Login Admin
```
POST http://localhost:8080/api/auth/admin/login
Content-Type: application/json

{
  "email": "admin@test.com",
  "password": "password123"
}
```

### Get Admin Dashboard
```
GET http://localhost:8080/api/admin/1/dashboard
Authorization: Bearer <<JWT_TOKEN>>
```

---

## Environment Variables

### Backend (.env or application.properties)
```
MYSQL_URL=jdbc:mysql://localhost:3306/filevault_db
MYSQL_USER=root
MYSQL_PASSWORD=password
JWT_SECRET=your-secret-key
JWT_EXPIRATION=86400000
FILE_UPLOAD_DIR=D:/filevault-uploads
```

### Frontend (.env)
```
REACT_APP_API_URL=http://localhost:8080/api
```

---

## Monitoring & Logs

### View Backend Logs
```bash
# In IDE console or:
tail -f backend/nohup.out

# Check specific error:
grep ERROR backend/nohup.out
```

### View Database Logs
```bash
# MySQL error log location
# Windows: C:\ProgramData\MySQL\MySQL Server 8.0\Data
# Linux: /var/log/mysql
```

---

## Performance Optimization

1. **Database Indexing**: Indexes are already created in schema.sql
2. **Caching**: Implement Spring Cache for frequently accessed data
3. **CDN**: Serve files through CDN in production
4. **Database Connection Pool**: Already configured in Spring Boot

---

## Next Steps

1. ✅ Complete Basic Setup
2. ✅ Test Authentication Flows
3. ✅ Upload Test Files
4. ✅ Test File Access Control
5. ⏭️ Implement Payment Gateway (Stripe/PayPal)
6. ⏭️ Add Advanced Analytics
7. ⏭️ Deploy to Production

---

## Support & Help

- Check logs for detailed error messages
- Verify all ports are available
- Ensure all credentials are correct
- Clear browser cache after config changes

For issues, check the main README.md or contact the development team.
