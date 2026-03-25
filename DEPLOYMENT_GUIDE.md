# Deployment Guide: Render (Backend) + Vercel (Frontend)

## **STEP 1: BACKEND DEPLOYMENT TO RENDER**

### 1.1 Prepare Backend for Production

#### A. Update application.properties for Production

Your backend needs environment variables for production. We'll configure these in Render's dashboard.

Key variables needed:
- Database URL
- Database Username/Password
- JWT Secret
- CORS Origins (Vercel frontend URL)

#### B. Update CorsConfig.java

The CORS configuration should accept the Vercel frontend URL. Check your file and ensure it's using environment variables:

```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] allowedOrigins = System.getenv("CORS_ORIGINS") != null 
            ? System.getenv("CORS_ORIGINS").split(",")
            : new String[]{"http://localhost:3000"};
            
        registry.addMapping("/api/**")
            .allowedOrigins(allowedOrigins)
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true);
    }
}
```

### 1.2 Deploy Backend to Render

#### Step-by-Step:

1. **Create Render Account**
   - Go to https://render.com
   - Sign up with GitHub

2. **Connect GitHub Repository**
   - Click "New" → "Web Service"
   - Select your GitHub repository: `ANJANIPRASAD01/Secure_Content_Sharing_and_Access_Management_System`
   - Connect GitHub to Render

3. **Configure Web Service**
   - **Name:** `filevault-backend`
   - **Region:** Select closest to you (e.g., Virginia, Frankfurt, Singapore)
   - **Branch:** `main`
   - **Runtime:** `Java 17`
   - **Build Command:** 
     ```
     cd backend && mvn clean package -DskipTests
     ```
   - **Start Command:**
     ```
     cd backend && java -jar target/filevault-1.0.0.jar
     ```
   - **Plan:** Free (or Paid if you need uptime guarantees)

4. **Set Environment Variables**
   Go to "Environment" section and add:
   
   ```
   PORT=8080
   SPRING_DATASOURCE_URL=jdbc:mysql://<your-db-host>:3306/filevault_db
   SPRING_DATASOURCE_USERNAME=<your-db-user>
   SPRING_DATASOURCE_PASSWORD=<your-db-password>
   JWT_SECRET=<your-strong-secret-key-min-32-chars>
   JWT_EXPIRATION=86400000
   CORS_ORIGINS=https://<your-vercel-frontend-url>
   SPRING_JPA_HIBERNATE_DDL_AUTO=update
   ```

5. **Database Setup**
   - Option A: Use free MySQL on Render (limited)
   - Option B: Use AWS RDS free tier
   - Option C: Use PlanetScale (MySQL serverless, free tier)
   
   **Recommended:** PlanetScale
   - Go to https://planetscale.com
   - Create account, create database
   - Copy connection string and use it in `SPRING_DATASOURCE_URL`

6. **Deploy**
   - Click "Create Web Service"
   - Wait for build to complete (5-10 minutes)
   - You'll get a URL like: `https://filevault-backend.onrender.com`

---

## **STEP 2: FRONTEND DEPLOYMENT TO VERCEL**

### 2.1 Prepare Frontend for Production

The frontend is already configured to use `REACT_APP_API_URL` environment variable.

### 2.2 Deploy Frontend to Vercel

#### Step-by-Step:

1. **Create Vercel Account**
   - Go to https://vercel.com
   - Sign up with GitHub

2. **Import Project**
   - Click "Add New" → "Project"
   - Select your GitHub repo: `ANJANIPRASAD01/Secure_Content_Sharing_and_Access_Management_System`
   - Click "Import"

3. **Configure Project**
   - **Project Name:** `filevault-frontend`
   - **Framework Preset:** React
   - **Root Directory:** `./frontend`

4. **Environment Variables**
   - Add the following under "Environment Variables":
   
   ```
   REACT_APP_API_URL=https://filevault-backend.onrender.com/api
   ```
   
   *(Replace with your actual Render backend URL)*

5. **Build Settings**
   - **Build Command:** `npm run build`
   - **Output Directory:** `build`
   - **Install Command:** `npm install`

6. **Deploy**
   - Click "Deploy"
   - Wait for build (2-3 minutes)
   - You'll get a URL like: `https://filevault-frontend.vercel.app`

---

## **STEP 3: VERIFY DEPLOYMENT**

### 3.1 Test Backend
```
curl https://filevault-backend.onrender.com/api/health
```

### 3.2 Test Frontend
- Visit: `https://filevault-frontend.vercel.app`
- Try logging in
- Test file upload/download

### 3.3 If You Get CORS Errors
Update `CORS_ORIGINS` in Render environment variables:
```
CORS_ORIGINS=https://filevault-frontend.vercel.app,https://www.filevault-frontend.vercel.app
```

---

## **STEP 4: CUSTOM DOMAIN (OPTIONAL)**

### For Backend:
- Go to Render → Settings → Custom Domain
- Add your domain and follow DNS instructions

### For Frontend:
- Go to Vercel → Settings → Domains
- Add your domain and follow DNS instructions

---

## **STEP 5: CONTINUOUS DEPLOYMENT (AUTO)**

Both platforms automatically redeploy when you push to `main` branch:

```bash
git add .
git commit -m "Production deployment"
git push origin main
```

---

## **TROUBLESHOOTING**

### Backend Won't Start on Render
- Check logs: Render Dashboard → Logs
- Common issues:
  - Database connection failed → Check `SPRING_DATASOURCE_URL`
  - Missing environment variables → Add them in Render dashboard
  - Port already in use → Render manages this, just ensure `PORT=8080`

### Frontend Shows Blank Page
- Check browser console (F12) for errors
- Verify `REACT_APP_API_URL` is set correctly
- Check if backend is responding

### API Calls Fail
- Check network tab in DevTools
- Verify CORS_ORIGINS includes frontend URL
- Check JWT_SECRET is set correctly

---

## **IMPORTANT: Production Checklist**

- [ ] Change `JWT_SECRET` to a strong random string
- [ ] Update database credentials (don't use localhost)
- [ ] Set `SPRING_JPA_HIBERNATE_DDL_AUTO=validate` (not update in production)
- [ ] Enable HTTPS (both Render and Vercel do this by default)
- [ ] Set up monitoring/alerts
- [ ] Backup database regularly
- [ ] Review security configurations in `SecurityConfig.java`
