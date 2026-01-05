# Job Board API Design

## Authentication Endpoints

### POST /api/auth/register
Register new user (candidate/recruiter)
```json
{
  "email": "user@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+1234567890",
  "role": "CANDIDATE" // or "RECRUITER"
}
```

### POST /api/auth/login
User login
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```
Response:
```json
{
  "accessToken": "jwt-token",
  "refreshToken": "refresh-token",
  "user": {
    "id": 1,
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "roles": ["CANDIDATE"]
  }
}
```

### POST /api/auth/refresh
Refresh access token

### POST /api/auth/logout
Logout user

## Candidate Endpoints

### GET /api/candidates/profile
Get candidate profile

### PUT /api/candidates/profile
Update candidate profile
```json
{
  "summary": "Experienced software developer...",
  "location": "San Francisco, CA",
  "linkedin": "linkedin.com/in/johndoe",
  "github": "github.com/johndoe",
  "website": "johndoe.dev"
}
```

### POST /api/candidates/resume
Upload resume (multipart/form-data)

### GET /api/candidates/resume
Get current resume details

### POST /api/candidates/education
Add education
```json
{
  "degree": "Bachelor of Science",
  "field": "Computer Science",
  "institution": "Stanford University",
  "startDate": "2018-09-01",
  "endDate": "2022-06-01",
  "gpa": 3.8
}
```

### POST /api/candidates/experience
Add work experience
```json
{
  "company": "Tech Corp",
  "position": "Software Engineer",
  "description": "Developed web applications...",
  "startDate": "2022-07-01",
  "endDate": "2024-01-01",
  "isCurrent": false
}
```

### GET /api/candidates/jobs/recommended
Get recommended jobs for candidate

### POST /api/candidates/jobs/{jobId}/apply
Apply for a job
```json
{
  "coverLetter": "I am interested in this position..."
}
```

### GET /api/candidates/applications
Get candidate's applications

## Recruiter Endpoints

### GET /api/recruiters/profile
Get recruiter profile

### PUT /api/recruiters/profile
Update recruiter profile

### POST /api/jobs
Create job posting
```json
{
  "title": "Senior Software Engineer",
  "description": "We are looking for...",
  "requirements": "5+ years experience...",
  "location": "San Francisco, CA",
  "salaryMin": 120000,
  "salaryMax": 180000,
  "jobType": "FULL_TIME",
  "remote": true,
  "expiresAt": "2024-12-31T23:59:59Z",
  "skills": [
    {
      "skillName": "Java",
      "required": true,
      "minYears": 5,
      "weight": 0.3
    },
    {
      "skillName": "Spring Boot",
      "required": true,
      "minYears": 3,
      "weight": 0.2
    }
  ]
}
```

### GET /api/jobs
Get recruiter's job postings

### GET /api/jobs/{jobId}
Get job details

### PUT /api/jobs/{jobId}
Update job posting

### DELETE /api/jobs/{jobId}
Delete job posting

### GET /api/jobs/{jobId}/candidates
Get ranked candidates for job
```json
{
  "candidates": [
    {
      "id": 1,
      "firstName": "John",
      "lastName": "Doe",
      "email": "john@example.com",
      "matchScore": 85.5,
      "skillScore": 90.0,
      "experienceScore": 80.0,
      "educationScore": 85.0,
      "appliedAt": "2024-01-15T10:30:00Z",
      "status": "APPLIED"
    }
  ]
}
```

### GET /api/jobs/{jobId}/applications
Get applications for job

### PUT /api/applications/{applicationId}/status
Update application status
```json
{
  "status": "REVIEWED" // APPLIED, REVIEWED, SHORTLISTED, REJECTED, HIRED
}
```

## Public Endpoints

### GET /api/jobs/search
Search jobs (public)
Query parameters:
- title: job title keyword
- location: location filter
- skills: comma-separated skills
- jobType: FULL_TIME, PART_TIME, CONTRACT
- remote: true/false
- salaryMin: minimum salary
- page: page number
- size: page size

### GET /api/jobs/{jobId}/public
Get public job details

## Admin Endpoints

### GET /api/admin/users
Get all users (paginated)

### PUT /api/admin/users/{userId}/status
Activate/deactivate user

### GET /api/admin/stats
Get platform statistics

## Common Response Format

### Success Response
```json
{
  "success": true,
  "data": { ... },
  "message": "Operation successful"
}
```

### Error Response
```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Invalid input data",
    "details": {
      "email": "Email is required"
    }
  }
}
```

### Paginated Response
```json
{
  "success": true,
  "data": {
    "content": [...],
    "page": 0,
    "size": 20,
    "totalElements": 100,
    "totalPages": 5,
    "first": true,
    "last": false
  }
}
```