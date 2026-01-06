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

### Option 1: Running with Docker Compose (Recommended)
```bash
# Clone the repository
git clone <repository-url>
cd job-board-platform

# Copy and configure environment variables
cp .env.sample .env
# Edit .env file with your configuration

# Start the application with Docker Compose
docker-compose up -d

# The application will be available at:
# - API: http://localhost:8080
# - Swagger UI: http://localhost:8080/swagger-ui.html
# - PostgreSQL: localhost:5432

# View logs
docker-compose logs -f app

# Stop the application
docker-compose down

# Stop and remove volumes (clears database)
docker-compose down -v
```

### Environment Configuration

The application uses environment variables for configuration. Copy `.env.sample` to `.env` and update the values:

```bash
# Copy sample environment file
cp .env.sample .env

# Edit the environment file
nano .env  # or use your preferred editor
```

**Key Environment Variables:**
- `POSTGRES_PASSWORD` - Database password (change in production)
- `JWT_SECRET` - JWT signing secret (must be at least 32 characters)
- `SPRING_PROFILES_ACTIVE` - Application profile (dev/prod)
- `FILE_UPLOAD_DIR` - Directory for resume uploads
- `JAVA_OPTS` - JVM memory settings

**Security Note:** Never commit `.env` files to version control. The `.env` file is already in `.gitignore`.

### Option 2: Building Docker Container Manually

#### Step 1: Build the Application JAR
```bash
# Clean and build the application
./mvnw clean package -DskipTests

# Verify the JAR file is created
ls -la target/job-board-platform-1.0.0.jar
```

#### Step 2: Build Docker Image
```bash
# Option 1: Build with the main Dockerfile (eclipse-temurin)
docker build -t job-board-platform:latest .

# Option 2: If eclipse-temurin doesn't work, use Amazon Corretto
docker build -f Dockerfile.corretto -t job-board-platform:latest .

# Option 3: Simple single-stage build
docker build -f Dockerfile.simple -t job-board-platform:latest .

# Option 4: Use the build script
chmod +x build-docker.sh
./build-docker.sh

# Verify the image is created
docker images | grep job-board-platform

# Optional: Tag for different environments
docker tag job-board-platform:latest job-board-platform:1.0.0
```

**Troubleshooting Docker Build Issues:**
```bash
# Issue 1: "no match for platform in manifest"
# Solution: Use Amazon Corretto which supports more platforms
docker build -f Dockerfile.corretto -t job-board-platform:latest .

# Issue 2: Build for specific platform (Apple Silicon users)
docker build --platform linux/amd64 -t job-board-platform:latest .

# Issue 3: Clear cache and rebuild
docker build --no-cache -t job-board-platform:latest .

# Issue 4: Check available Java images
docker search eclipse-temurin
docker search amazoncorretto

# Issue 5: Build locally first, then containerize
./mvnw clean package -DskipTests
docker build -t job-board-platform:latest .
```

#### Step 3: Run with Docker Network
```bash
# Create a custom network
docker network create jobboard-network

# Run PostgreSQL container
docker run -d \
  --name jobboard-postgres \
  --network jobboard-network \
  -e POSTGRES_DB=jobboard \
  -e POSTGRES_USER=jobboard \
  -e POSTGRES_PASSWORD=jobboard123 \
  -p 5432:5432 \
  -v jobboard_postgres_data:/var/lib/postgresql/data \
  postgres:15-alpine

# Wait for PostgreSQL to start (about 10-15 seconds)
sleep 15

# Run the application container
docker run -d \
  --name jobboard-app \
  --network jobboard-network \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://jobboard-postgres:5432/jobboard \
  -e SPRING_DATASOURCE_USERNAME=jobboard \
  -e SPRING_DATASOURCE_PASSWORD=jobboard123 \
  -p 8080:8080 \
  -v $(pwd)/uploads:/app/uploads \
  job-board-platform:latest

# Check application logs
docker logs -f jobboard-app
```

#### Step 4: Verify Deployment
```bash
# Check if containers are running
docker ps

# Test the API
curl http://localhost:8080/actuator/health

# Access Swagger UI
open http://localhost:8080/swagger-ui.html
```

### Option 3: Running Locally (Development)
```bash
# Start PostgreSQL database only
docker run -d --name postgres \
  -e POSTGRES_DB=jobboard \
  -e POSTGRES_USER=jobboard \
  -e POSTGRES_PASSWORD=jobboard123 \
  -p 5432:5432 postgres:15-alpine

# Run the application locally
./mvnw spring-boot:run

# Or build and run JAR
./mvnw clean package
java -jar target/job-board-platform-1.0.0.jar
```

### Docker Management Commands

#### Container Management
```bash
# View running containers
docker ps

# View all containers (including stopped)
docker ps -a

# Stop containers
docker stop jobboard-app jobboard-postgres

# Remove containers
docker rm jobboard-app jobboard-postgres

# Remove network
docker network rm jobboard-network
```

#### Image Management
```bash
# List images
docker images

# Remove image
docker rmi job-board-platform:latest

# Clean up unused images
docker image prune

# Clean up everything (use with caution)
docker system prune -a
```

#### Volume Management
```bash
# List volumes
docker volume ls

# Remove specific volume
docker volume rm jobboard_postgres_data

# Remove unused volumes
docker volume prune
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

## Deployment

### Docker Deployment Options

#### Production Docker Compose
For production deployment:

```bash
# Copy production environment template
cp .env.prod .env

# Edit with your production values
nano .env

# IMPORTANT: Update these values in production:
# - POSTGRES_PASSWORD: Use a strong password
# - JWT_SECRET: Use a secure random string (32+ characters)
# - JAVA_OPTS: Adjust memory settings for your server

# Deploy with production compose file
docker-compose -f docker-compose.prod.yml up -d
```

**Production Environment Variables:**
```bash
# Database - Use strong credentials
POSTGRES_PASSWORD=your_secure_production_password

# JWT - Use a secure random secret
JWT_SECRET=your_production_jwt_secret_key_at_least_32_characters_long

# Memory - Adjust for your server
JAVA_OPTS=-Xmx1024m -Xms512m
```

#### Multi-Stage Docker Build
For optimized production builds, update the Dockerfile:

```dockerfile
# Multi-stage build for production
FROM openjdk:17-jdk-slim as builder

WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Download dependencies
RUN ./mvnw dependency:go-offline

# Copy source code
COPY src ./src

# Build application
RUN ./mvnw clean package -DskipTests

# Production stage
FROM openjdk:17-jre-slim

WORKDIR /app

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Copy JAR from builder stage
COPY --from=builder /app/target/job-board-platform-1.0.0.jar app.jar

# Create uploads directory
RUN mkdir -p /app/uploads/resumes

# Create non-root user
RUN addgroup --system spring && adduser --system spring --ingroup spring
RUN chown -R spring:spring /app
USER spring:spring

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### Container Registry Deployment
```bash
# Build and tag for registry
docker build -t your-registry.com/job-board-platform:1.0.0 .
docker build -t your-registry.com/job-board-platform:latest .

# Push to registry
docker push your-registry.com/job-board-platform:1.0.0
docker push your-registry.com/job-board-platform:latest

# Pull and run from registry
docker pull your-registry.com/job-board-platform:latest
docker run -d --name jobboard-app your-registry.com/job-board-platform:latest
```

### Kubernetes Deployment
Create Kubernetes manifests:

```yaml
# k8s/namespace.yaml
apiVersion: v1
kind: Namespace
metadata:
  name: jobboard

---
# k8s/postgres.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: postgres
  namespace: jobboard
spec:
  replicas: 1
  selector:
    matchLabels:
      app: postgres
  template:
    metadata:
      labels:
        app: postgres
    spec:
      containers:
      - name: postgres
        image: postgres:15-alpine
        env:
        - name: POSTGRES_DB
          value: jobboard
        - name: POSTGRES_USER
          value: jobboard
        - name: POSTGRES_PASSWORD
          valueFrom:
            secretKeyRef:
              name: postgres-secret
              key: password
        ports:
        - containerPort: 5432
        volumeMounts:
        - name: postgres-storage
          mountPath: /var/lib/postgresql/data
      volumes:
      - name: postgres-storage
        persistentVolumeClaim:
          claimName: postgres-pvc

---
# k8s/app.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: jobboard-app
  namespace: jobboard
spec:
  replicas: 3
  selector:
    matchLabels:
      app: jobboard-app
  template:
    metadata:
      labels:
        app: jobboard-app
    spec:
      containers:
      - name: app
        image: job-board-platform:latest
        env:
        - name: SPRING_DATASOURCE_URL
          value: jdbc:postgresql://postgres-service:5432/jobboard
        - name: SPRING_DATASOURCE_USERNAME
          value: jobboard
        - name: SPRING_DATASOURCE_PASSWORD
          valueFrom:
            secretKeyRef:
              name: postgres-secret
              key: password
        ports:
        - containerPort: 8080
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
```

Deploy to Kubernetes:
```bash
# Apply manifests
kubectl apply -f k8s/

# Check deployment
kubectl get pods -n jobboard
kubectl get services -n jobboard

# View logs
kubectl logs -f deployment/jobboard-app -n jobboard
```

### Production Considerations
- Use external PostgreSQL database (AWS RDS, Google Cloud SQL, etc.)
- Configure proper JWT secrets and rotate them regularly
- Set up SSL/TLS termination with reverse proxy (Nginx, Traefik)
- Configure file storage (AWS S3, Google Cloud Storage, etc.)
- Set up monitoring and alerting (Prometheus, Grafana)
- Configure backup strategies for database and uploaded files
- Use container orchestration (Kubernetes, Docker Swarm)
- Implement CI/CD pipelines for automated deployments
- Configure log aggregation (ELK stack, Fluentd)
- Set up health checks and auto-scaling

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