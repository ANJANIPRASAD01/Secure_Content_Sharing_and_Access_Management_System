# RENDER ENVIRONMENT VARIABLES - Copy these to Render Dashboard

# ================================================================
# REQUIRED - Database Connection
# ================================================================
PORT=8080
SPRING_DATASOURCE_URL=jdbc:mysql://your-planetscale-host:3306/filevault_db
SPRING_DATASOURCE_USERNAME=your_db_username
SPRING_DATASOURCE_PASSWORD=your_db_password

# ================================================================
# REQUIRED - Security (JWT)
# Generate a strong random string at least 32 characters
# Use: openssl rand -base64 32
# ================================================================
JWT_SECRET=YOUR_RANDOM_SECRET_KEY_AT_LEAST_32_CHARS_HERE
JWT_EXPIRATION=86400000

# ================================================================
# REQUIRED - CORS (Frontend URL from Vercel)
# ================================================================
CORS_ORIGINS=https://your-frontend.vercel.app

# ================================================================
# OPTIONAL - Logging (Default: INFO)
# ================================================================
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_COM_FILEVAULT=DEBUG

# ================================================================
# OPTIONAL - File Upload (Default: 100MB)
# ================================================================
SPRING_SERVLET_MULTIPART_MAX_FILE_SIZE=100MB
SPRING_SERVLET_MULTIPART_MAX_REQUEST_SIZE=100MB

# ================================================================
# OPTIONAL - JPA Configuration
# ================================================================
SPRING_JPA_HIBERNATE_DDL_AUTO=update
SPRING_JPA_SHOW_SQL=false

# ================================================================
# OPTIONAL - Java Memory (for free tier instances)
# ================================================================
JAVA_OPTS=-Xmx512m
