# PostgreSQL Deployment Guide - Render + Railway

## **Option 1: Railway PostgreSQL (Recommended - Easier)**

### **Step 1: Create Railway Account**
- Go to https://railway.app
- Sign up with GitHub

### **Step 2: Create PostgreSQL Database**
1. Click **"Create New"** → **"Database"** → **"PostgreSQL"**
2. Database auto-created with credentials

### **Step 3: Get Connection String**
1. In Railway Dashboard, select your PostgreSQL database
2. Click **"Connect"**
3. Copy the connection URL, looks like:

```
postgresql://user:password@region.railway.app:port/database
```

### **Step 4: Format for Spring Boot (Java)**

Convert URL to:
```
jdbc:postgresql://region.railway.app:5432/railway?sslmode=require
```

Extract:
- **URL:** `jdbc:postgresql://region.railway.app:5432/railway?sslmode=require`
- **Username:** From Railway credentials
- **Password:** From Railway credentials

---

## **Option 2: Render PostgreSQL**

### **Step 1: Create PostgreSQL on Render**
1. Go to https://render.com → Dashboard
2. Click **"New +"** → **"PostgreSQL"**
3. Name: `filevault-postgres`
4. Create database

### **Step 2: Get Connection Info**
1. Database created → Internal Database URL shows
2. Copy the connection details

---

## **Update Render Backend Service**

Go to your Render backend service → **Environment** → Update variables:

| Name | Value |
|------|-------|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://region.railway.app:5432/railway?sslmode=require` |
| `SPRING_DATASOURCE_USERNAME` | From Railway/Render credentials |
| `SPRING_DATASOURCE_PASSWORD` | From Railway/Render password |
| `JWT_SECRET` | Your 32+ char secret key |
| `JWT_EXPIRATION` | `86400000` |
| `CORS_ORIGINS` | `https://your-vercel-frontend.vercel.app` |
| `LOGGING_LEVEL_ROOT` | `INFO` |
| `LOGGING_LEVEL_COM_FILEVAULT` | `DEBUG` |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | `update` |

---

## **Test Connection**

After updating, Render will auto-redeploy.

**Expected logs:**
```
HikariPool-1 - Successfully connected to database
Started FileVaultApplication in X seconds
```

---

## **Troubleshooting PostgreSQL Errors**

### **"Connection refused"**
- Database might not be ready
- Wait 1-2 minutes and redeploy

### **"SSL connection error"**
- Add `?sslmode=require` to connection URL
- Or `?sslAccept=strict`

### **"Unknown host"**
- Connection string format is wrong
- Double-check Railway/Render URL format

---

## **Summary: PostgreSQL vs MySQL**

| Feature | MySQL | PostgreSQL |
|---------|-------|-----------|
| **Performance** | Good | Better |
| **JSON Support** | Limited | Excellent |
| **ACID Compliance** | Good | Excellent |
| **Scalability** | Good | Best |
| **Free Tier** | Yes | Yes |
| **Recommended** | Legacy | ✅ Modern |

We're using PostgreSQL now! 🚀
