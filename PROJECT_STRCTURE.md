# FileVault - Project Structure

```
FileVault/
│
├── backend/                                    # Spring Boot Application
│   ├── pom.xml                                # Maven configuration
│   └── src/
│       ├── main/
│       │   ├── java/com/filevault/
│       │   │   ├── FileVaultApplication.java  # Main Spring Boot class
│       │   │   │
│       │   │   ├── config/
│       │   │   │   ├── SecurityConfig.java         # Spring Security & JWT config
│       │   │   │   ├── JwtConfig.java              # JWT configuration
│       │   │   │   └── CorsConfig.java             # CORS configuration
│       │   │   │
│       │   │   ├── controller/
│       │   │   │   ├── AuthController.java         # Login/Register for both roles
│       │   │   │   ├── AdminController.java        # Admin/uploader endpoints
│       │   │   │   ├── UserController.java         # User/viewer endpoints
│       │   │   │   └── FileController.java         # File upload/download
│       │   │   │
│       │   │   ├── service/
│       │   │   │   ├── AuthService.java            # Authentication logic
│       │   │   │   ├── AdminService.java           # Admin business logic
│       │   │   │   ├── UserService.java            # User business logic
│       │   │   │   ├── FileService.java            # File handling
│       │   │   │   ├── AccessControlService.java   # Access management
│       │   │   │   └── PaymentService.java         # Payment processing
│       │   │   │
│       │   │   ├── entity/
│       │   │   │   ├── Admin.java                  # Admin entity
│       │   │   │   ├── User.java                   # User entity
│       │   │   │   ├── File.java                   # File metadata entity
│       │   │   │   ├── Category.java               # Category entity
│       │   │   │   ├── AccessControl.java          # Access control entity
│       │   │   │   └── Payment.java                # Payment transaction entity
│       │   │   │
│       │   │   ├── repository/
│       │   │   │   ├── AdminRepository.java        # Admin data access
│       │   │   │   ├── UserRepository.java         # User data access
│       │   │   │   ├── FileRepository.java         # File data access
│       │   │   │   ├── CategoryRepository.java     # Category data access
│       │   │   │   ├── AccessControlRepository.java # Access control data
│       │   │   │   └── PaymentRepository.java      # Payment data access
│       │   │   │
│       │   │   ├── dto/
│       │   │   │   ├── LoginRequest.java           # Login DTO
│       │   │   │   ├── RegisterRequest.java        # Register DTO
│       │   │   │   ├── JwtResponse.java            # JWT response DTO
│       │   │   │   ├── FileUploadRequest.java      # File upload DTO
│       │   │   │   ├── AccessGrantRequest.java     # Access grant DTO
│       │   │   │   └── PaymentRequest.java         # Payment DTO
│       │   │   │
│       │   │   ├── security/
│       │   │   │   ├── JwtProvider.java            # JWT token generation/validation
│       │   │   │   ├── CustomUserDetailsService.java # User details service
│       │   │   │   └── JwtAuthenticationFilter.java  # JWT request filter
│       │   │   │
│       │   │   └── exception/
│       │   │       ├── ResourceNotFoundException.java
│       │   │       ├── UnauthorizedAccessException.java
│       │   │       └── GlobalExceptionHandler.java
│       │   │
│       │   └── resources/
│       │       ├── application.properties         # Main config
│       │       ├── application-dev.properties     # Dev config
│       │       └── application-prod.properties    # Production config
│       │
│       └── test/
│           └── java/com/filevault/
│
├── frontend/                                   # React Application
│   ├── package.json
│   ├── public/
│   │   └── index.html
│   └── src/
│       ├── components/
│       │   ├── Auth/
│       │   │   ├── AdminLogin.jsx
│       │   │   ├── AdminRegister.jsx
│       │   │   ├── UserLogin.jsx
│       │   │   └── UserRegister.jsx
│       │   ├── Admin/
│       │   │   ├── AdminDashboard.jsx
│       │   │   ├── FileUpload.jsx
│       │   │   ├── MyFiles.jsx
│       │   │   ├── AccessControl.jsx
│       │   │   └── FileDetail.jsx
│       │   ├── User/
│       │   │   ├── UserDashboard.jsx
│       │   │   ├── BrowseFiles.jsx
│       │   │   ├── MyPurchases.jsx
│       │   │   └── FileViewer.jsx
│       │   └── Shared/
│       │       ├── Navbar.jsx
│       │       ├── CategoryFilter.jsx
│       │       └── FileCard.jsx
│       ├── App.jsx
│       ├── index.jsx
│       ├── api/
│       │   └── axiosConfig.js
│       ├── pages/
│       │   ├── HomePage.jsx
│       │   ├── NotFound.jsx
│       │   └── Unauthorized.jsx
│       └── styles/

├── database/
│   ├── schema.sql                              # Database schema
│   └── seed_data.sql                           # Initial data
│
└── README.md                                   # Project documentation
```

## Key Directories Explained:

- **backend/**: Complete Spring Boot application with all layers
- **frontend/**: React-based admin & user dashboards
- **database/**: SQL scripts for setup
- **config/**: Configuration for security, JWT, CORS
- **service/**: Business logic layer
- **entity/**: JPA entities mapped to database
- **repository/**: Data access layer using Spring Data JPA
- **dto/**: Data transfer objects for API communication
- **security/**: JWT and authentication implementation
- **exception/**: Custom exception handling
