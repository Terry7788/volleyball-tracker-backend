# 🏐 Volleyball Tracker - Backend API

A robust Spring Boot REST API for managing volleyball matches with user authentication, guest sessions, and real-time match tracking capabilities.

[![Live API](https://img.shields.io/badge/Live%20API-Railway-0B0D0E?style=for-the-badge&logo=railway)](https://volleyball-tracker-backend-production.up.railway.app)
[![GitHub](https://img.shields.io/badge/GitHub-Backend-181717?style=for-the-badge&logo=github)](https://github.com/Terry7788/volleyball-tracker-backend)

## ✨ Features

### 🔐 Authentication System
- **JWT Authentication**: Secure token-based user sessions
- **Guest Sessions**: 24-hour temporary sessions without registration
- **User Registration**: Username, email, and password management
- **Session Management**: Automatic cleanup of expired guest sessions

### 🏐 Match Management
- **Real-time Scoring**: Track volleyball matches with official rules
- **Match States**: In-progress, completed, and paused matches
- **Set Management**: Complete set history with editing capabilities
- **Score Validation**: Proper volleyball scoring rules enforcement

### 📊 Data Management
- **PostgreSQL Database**: Reliable data persistence
- **JPA/Hibernate**: Object-relational mapping
- **Data Validation**: Input validation and error handling
- **CORS Support**: Cross-origin requests for web clients

### 🛡️ Security & Performance
- **Spring Security**: Authentication and authorization
- **Password Encryption**: BCrypt password hashing
- **Rate Limiting**: Protection against abuse
- **Error Handling**: Comprehensive exception management

## 🚀 Live API

**Base URL**: `https://volleyball-tracker-backend-production.up.railway.app`

### Quick Test
```bash
# Create a guest session
curl -X POST https://volleyball-tracker-backend-production.up.railway.app/api/guest/session

# Test with the returned sessionId
curl -X POST \
  https://volleyball-tracker-backend-production.up.railway.app/api/matches \
  -H "Content-Type: application/json" \
  -H "Guest-Session-Id: YOUR_SESSION_ID" \
  -d '{"team1Name": "Lakers", "team2Name": "Warriors"}'
```

## 🛠️ Tech Stack

### Backend Framework
- **Spring Boot 3.5.4** - Enterprise Java framework
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - Data persistence layer
- **Spring Web** - REST API endpoints

### Database
- **PostgreSQL** - Production database
- **H2** - Development and testing database
- **Hibernate** - ORM framework

### Security
- **JWT (JSON Web Tokens)** - Stateless authentication
- **BCrypt** - Password hashing
- **CORS** - Cross-origin resource sharing

### Development Tools
- **Maven** - Dependency management and build
- **Lombok** - Reduce boilerplate code
- **Java 17** - Latest LTS Java version

## 📦 Installation

### Prerequisites
- Java 17+
- Maven 3.6+
- PostgreSQL 12+ (for production)
- Git

### Local Development

1. **Clone the repository**
   ```bash
   git clone https://github.com/Terry7788/volleyball-tracker-backend.git
   cd volleyball-tracker-backend
   ```

2. **Configure database (Optional - uses H2 by default)**
   ```bash
   # Create application-local.properties
   echo "spring.datasource.url=jdbc:postgresql://localhost:5432/volleyball_tracker" > src/main/resources/application-local.properties
   echo "spring.datasource.username=your_username" >> src/main/resources/application-local.properties
   echo "spring.datasource.password=your_password" >> src/main/resources/application-local.properties
   ```

3. **Run the application**
   ```bash
   # Using Maven
   ./mvnw spring-boot:run
   
   # Or with specific profile
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
   ```

4. **Verify it's running**
   ```bash
   curl http://localhost:8080/api/guest/session
   ```

## 🏗️ Project Structure

```
src/main/java/com/volleyball/scoretracker/
├── ScoretrackerApplication.java    # Main application class
├── config/                         # Configuration classes
│   └── SecurityConfig.java        # Security configuration
├── controller/                     # REST controllers
│   ├── AuthController.java        # Authentication endpoints
│   ├── GuestController.java       # Guest session management
│   └── MatchController.java       # Match management endpoints
├── dto/                           # Data Transfer Objects
│   ├── AuthResponse.java         # Authentication responses
│   ├── CreateMatchRequest.java   # Match creation requests
│   └── ...                       # Other DTOs
├── model/                         # JPA entities
│   ├── User.java                 # User entity
│   ├── Match.java                # Match entity
│   ├── SetScore.java             # Set score entity
│   └── GuestSession.java        # Guest session entity
├── repository/                    # Data access layer
│   ├── UserRepository.java      # User data access
│   ├── MatchRepository.java     # Match data access
│   └── ...                      # Other repositories
├── security/                      # Security components
│   ├── JwtUtils.java            # JWT utilities
│   ├── JwtAuthenticationFilter.java # JWT filter
│   └── UserDetailsImpl.java    # User details implementation
└── service/                       # Business logic
    ├── UserService.java         # User management
    ├── MatchService.java        # Match management
    ├── GuestSessionService.java # Guest session handling
    └── CleanupService.java      # Automated cleanup
```

## 🔌 API Endpoints

### Authentication
```http
POST /api/auth/register     # Register new user
POST /api/auth/login        # User login
POST /api/auth/validate     # Validate JWT token
```

### Guest Sessions
```http
POST /api/guest/session              # Create guest session
GET /api/guest/session/{id}/validate # Validate guest session
```

### Match Management
```http
GET /api/matches           # Get all user matches
POST /api/matches          # Create new match
GET /api/matches/{id}      # Get specific match
PUT /api/matches/{id}/score # Update match score
PUT /api/matches/{id}/undo  # Undo last point
PUT /api/matches/{id}/reset-set # Reset current set
DELETE /api/matches/{id}    # Delete match
```

### Example Requests

**Create Match (Authenticated User)**:
```bash
curl -X POST http://localhost:8080/api/matches \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{"team1Name": "Spiksters", "team2Name": "Net Ninjas"}'
```

**Update Score (Guest)**:
```bash
curl -X PUT http://localhost:8080/api/matches/1/score \
  -H "Content-Type: application/json" \
  -H "Guest-Session-Id: YOUR_SESSION_ID" \
  -d '{"team": "team1"}'
```

## 🗄️ Database Schema

### Users Table
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    user_type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    last_login_at TIMESTAMP
);
```

### Matches Table
```sql
CREATE TABLE matches (
    id BIGSERIAL PRIMARY KEY,
    team1_name VARCHAR(255) NOT NULL,
    team2_name VARCHAR(255) NOT NULL,
    team1_score INTEGER NOT NULL DEFAULT 0,
    team2_score INTEGER NOT NULL DEFAULT 0,
    team1_sets INTEGER NOT NULL DEFAULT 0,
    team2_sets INTEGER NOT NULL DEFAULT 0,
    current_set INTEGER NOT NULL DEFAULT 1,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    user_id BIGINT REFERENCES users(id),
    guest_session_id BIGINT REFERENCES guest_sessions(id)
);
```

### Set Scores Table
```sql
CREATE TABLE set_scores (
    id BIGSERIAL PRIMARY KEY,
    set_number INTEGER NOT NULL,
    team1_points INTEGER NOT NULL,
    team2_points INTEGER NOT NULL,
    match_id BIGINT NOT NULL REFERENCES matches(id)
);
```

## ⚙️ Configuration

### Application Properties
```properties
# Database Configuration
spring.datasource.url=${DATABASE_URL:jdbc:h2:mem:testdb}
spring.datasource.username=${DATABASE_USERNAME:sa}
spring.datasource.password=${DATABASE_PASSWORD:}

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

# JWT Configuration
app.jwtSecret=${JWT_SECRET:volleyballSecretKey}
app.jwtExpirationMs=${JWT_EXPIRATION:86400000}

# Server Configuration
server.port=${PORT:8080}
```

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DATABASE_URL` | PostgreSQL connection string | H2 in-memory |
| `DATABASE_USERNAME` | Database username | `sa` |
| `DATABASE_PASSWORD` | Database password | (empty) |
| `JWT_SECRET` | JWT signing secret | `volleyballSecretKey` |
| `JWT_EXPIRATION` | JWT expiration time (ms) | `86400000` (24h) |
| `PORT` | Server port | `8080` |

## 🚀 Deployment

### Railway (Current Production)

1. **Connect GitHub Repository**
   - Link your GitHub repository to Railway
   - Select the main branch for deployment

2. **Set Environment Variables**
   ```bash
   DATABASE_URL=postgresql://user:pass@host:port/dbname
   JWT_SECRET=your-super-secret-jwt-key-here
   JWT_EXPIRATION=86400000
   ```

3. **Deploy**
   - Railway automatically builds and deploys
   - Provides a public URL for your API

### Docker Deployment
```dockerfile
FROM openjdk:17-jdk-slim
COPY target/scoretracker-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

```bash
# Build and run with Docker
./mvnw clean package -DskipTests
docker build -t volleyball-tracker-backend .
docker run -p 8080:8080 volleyball-tracker-backend
```

### Heroku Deployment
```bash
# Install Heroku CLI and login
heroku create volleyball-tracker-api
heroku config:set JWT_SECRET=your-secret-key
heroku addons:create heroku-postgresql:mini
git push heroku main
```

## 🧪 Testing

### Run Tests
```bash
# Run all tests
./mvnw test

# Run with coverage
./mvnw test jacoco:report

# Run specific test class
./mvnw test -Dtest=MatchServiceTest
```

### API Testing with cURL
```bash
# Health check
curl http://localhost:8080/actuator/health

# Create guest session
curl -X POST http://localhost:8080/api/guest/session

# Register user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"test@example.com","password":"password123"}'
```

## 🔒 Security Features

### Authentication Flow
1. **User Registration/Login** → JWT Token issued
2. **Guest Session** → Temporary session ID (24h expiry)
3. **API Requests** → Validated via JWT or Guest Session ID
4. **Automatic Cleanup** → Expired sessions removed hourly

### Security Measures
- **Password Hashing**: BCrypt with salt
- **JWT Validation**: Signature and expiration checks
- **CORS Configuration**: Restricted to frontend domains
- **Input Validation**: Request data validation
- **SQL Injection Protection**: JPA/Hibernate parameterized queries

## 📊 Performance & Monitoring

### Automated Tasks
- **Guest Session Cleanup**: Runs every hour
- **Database Connection Pooling**: HikariCP
- **JVM Optimization**: Production-ready defaults

### Monitoring
```bash
# Health check endpoint
GET /actuator/health

# Application metrics
GET /actuator/metrics

# Database status
GET /actuator/info
```

## 🤝 Contributing

1. **Fork the repository**
2. **Create feature branch**
   ```bash
   git checkout -b feature/new-feature
   ```
3. **Write tests** for new functionality
4. **Commit changes**
   ```bash
   git commit -m 'Add new feature'
   ```
5. **Push to branch**
   ```bash
   git push origin feature/new-feature
   ```
6. **Create Pull Request**

### Development Guidelines
- Follow Spring Boot best practices
- Write unit and integration tests
- Use proper HTTP status codes
- Document API changes
- Maintain backward compatibility

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- **Spring Team** - For the excellent Spring Boot framework
- **Railway** - For reliable cloud hosting
- **PostgreSQL** - For robust database management
- **JWT.io** - For JWT implementation guidance

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/Terry7788/volleyball-tracker-backend/issues)
- **API Documentation**: Available at `/swagger-ui.html` (when enabled)
- **Email**: api-support@volleyballtracker.com

---

**Built with ☕ and ❤️ for volleyball score tracking** 🏐