# Job Board + Resume Matching Platform

A comprehensive job board and resume matching platform built with Spring Boot and PostgreSQL. The platform provides rule-based matching between candidates and job postings without using AI/ML models.

## Features

### User Management
- **Multi-role authentication**: Candidate, Recruiter, Admin roles
- **JWT-based security** with refresh tokens
- **Role-based access control (RBAC)**

### Candidate Features
- User registration and profile management
- Resume upload and parsing (PDF support via Apache Tika)
- Automatic skill extraction and experience parsing
- Job search and application management
- Personalized job recommendations based on skills and experience

### Recruiter Features
- Job posting creation and management
- Skill requirements definition with weights and minimum experience
- Candidate ranking based on match scores
- Application management and status tracking

### Matching Engine
- **Rule-based scoring algorithm** (no AI/ML)
- **Skill matching** with overlap percentage and experience weighting
- **Experience scoring** based on total years and job requirements
- **Education level matching**
- **Weighted scoring** with configurable skill importance
- **Detailed match explanations** stored in JSONB format

### Technical Features
- **PostgreSQL** with JSONB support and full-text search
- **Flyway** database migrations
- **Docker** containerization
- **Swagger/OpenAPI** documentation
- **Testcontainers** for integration testing
- **Global exception handling**
- **Input validation**

## Architecture

### Layered Architecture
```
├── Controller Layer    # REST API endpoints
├── Service Layer      # Business logic
├── Repository Layer   # Data access
├── Domain Layer       # JPA entities
├── DTO Layer         # Data transfer objects
├── Security Layer    # JWT authentication & authorization
└── Exception Layer   # Global exception handling
```

### Database Schema
- **Users & Roles**: Multi-role user management
- **Candidates**: Extended profiles with resumes, education, experience
- **Recruiters**: Company profiles and job management
- **Jobs & Skills**: Job postings with skill requirements
- **Applications**: Job applications with match scores
- **Match Scores**: Detailed scoring with JSONB metadata

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.6+
- Docker & Docker Compose
- PostgreSQL 13+ (if running locally)

### Running with Docker
```bash
# Clone the repository
git clone <repository-url>
cd job-board-platform

# Start the application with Docker Compose
docker-compose up -d

# The application will be available at:
# - API: http://localhost:8080
# - Swagger UI: http://localhost:8080/swagger-ui.html
```

### Running Locally
```bash
# Start PostgreSQL database
docker run -d --name postgres \
  -e POSTGRES_DB=jobboard \
  -e POSTGRES_USER=jobboard \
  -e POSTGRES_PASSWORD=jobboard123 \
  -p 5432:5432 postgres:15-alpine

# Run the application
./mvnw spring-boot:run

# Or build and run JAR
./mvnw clean package
java -jar target/job-board-platform-1.0.0.jar
```

## API Documentation

### Authentication Endpoints
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - User login
- `POST /api/auth/refresh` - Refresh JWT token
- `POST /api/auth/logout` - User logout

### Candidate Endpoints
- `GET /api/candidates/profile` - Get candidate profile
- `PUT /api/candidates/profile` - Update candidate profile
- `POST /api/candidates/resume` - Upload resume
- `POST /api/candidates/education` - Add education
- `POST /api/candidates/experience` - Add work experience
- `GET /api/candidates/jobs/recommended` - Get recommended jobs
- `POST /api/candidates/jobs/{jobId}/apply` - Apply for job

### Recruiter Endpoints
- `POST /api/jobs` - Create job posting
- `GET /api/jobs` - Get recruiter's jobs
- `GET /api/jobs/{jobId}/candidates` - Get ranked candidates
- `PUT /api/applications/{applicationId}/status` - Update application status

### Public Endpoints
- `GET /api/jobs/search` - Search jobs (public)
- `GET /api/jobs/{jobId}/public` - Get public job details

## Matching Algorithm

The platform uses a rule-based matching algorithm with three main components:

### 1. Skill Matching (50% weight)
- Calculates overlap between candidate skills and job requirements
- Considers skill experience levels and minimum requirements
- Applies penalties for missing required skills
- Supports weighted skill importance

### 2. Experience Matching (30% weight)
- Compares total work experience with job requirements
- Estimates required experience from job descriptions
- Provides partial scoring for candidates with less experience

### 3. Education Matching (20% weight)
- Compares education levels (Associate, Bachelor's, Master's, PhD)
- Extracts education requirements from job descriptions
- Provides full score when requirements are met or exceeded

### Match Score Calculation
```
Total Score = (Skill Score × 0.5) + (Experience Score × 0.3) + (Education Score × 0.2)
```

## Database Schema

### Key Tables
- `users` - User authentication and basic info
- `roles` - User roles (CANDIDATE, RECRUITER, ADMIN)
- `candidates` - Extended candidate profiles
- `resumes` - Resume files and parsed content
- `resume_skills` - Extracted skills with proficiency levels
- `jobs` - Job postings with requirements
- `job_skills` - Required skills with weights and minimum experience
- `applications` - Job applications with match scores
- `match_scores` - Detailed scoring breakdown

### Indexes
- Full-text search indexes on job titles and descriptions
- B-tree indexes on frequently queried columns
- Composite indexes for complex queries

## Testing

### Unit Tests
```bash
./mvnw test
```

### Integration Tests with Testcontainers
```bash
./mvnw test -Dtest=**/*IntegrationTest
```

### Test Coverage
```bash
./mvnw jacoco:report
```

## Configuration

### Application Properties
Key configuration options in `application.yml`:

```yaml
jwt:
  secret: your-secret-key
  expiration: 86400000  # 24 hours
  refresh-expiration: 604800000  # 7 days

file:
  upload:
    dir: ./uploads/resumes

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/jobboard
    username: jobboard
    password: jobboard123
```

### Environment Variables
- `SPRING_DATASOURCE_URL` - Database URL
- `SPRING_DATASOURCE_USERNAME` - Database username
- `SPRING_DATASOURCE_PASSWORD` - Database password
- `JWT_SECRET` - JWT signing secret
- `FILE_UPLOAD_DIR` - Resume upload directory

## Security

### Authentication
- JWT-based authentication with access and refresh tokens
- Password encryption using BCrypt
- Token expiration and refresh mechanism

### Authorization
- Role-based access control (RBAC)
- Method-level security annotations
- Resource-level access control

### Data Protection
- Input validation and sanitization
- SQL injection prevention via JPA
- XSS protection through proper encoding

## Monitoring & Observability

### Health Checks
- Spring Boot Actuator health endpoints
- Database connectivity checks
- Custom health indicators

### Logging
- Structured logging with SLF4J and Logback
- Request/response logging
- Error tracking and alerting

### Metrics
- Application metrics via Micrometer
- Database performance metrics
- Custom business metrics

## Deployment

### Docker Deployment
```bash
# Build and deploy with Docker Compose
docker-compose up -d

# Scale the application
docker-compose up -d --scale app=3
```

### Production Considerations
- Use external PostgreSQL database
- Configure proper JWT secrets
- Set up SSL/TLS termination
- Configure file storage (S3, etc.)
- Set up monitoring and alerting
- Configure backup strategies

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Ensure all tests pass
6. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For support and questions:
- Create an issue in the GitHub repository
- Check the API documentation at `/swagger-ui.html`
- Review the database schema in `/database-schema.md`