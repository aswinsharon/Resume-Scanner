# Job Board + Resume Matching Platform - Database Schema

## Entity Relationship Diagram

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│    users    │    │    roles    │    │ user_roles  │
├─────────────┤    ├─────────────┤    ├─────────────┤
│ id (PK)     │    │ id (PK)     │    │ user_id (FK)│
│ email       │    │ name        │    │ role_id (FK)│
│ password    │    │ description │    └─────────────┘
│ first_name  │    └─────────────┘
│ last_name   │
│ phone       │
│ created_at  │
│ updated_at  │
│ is_active   │
└─────────────┘

┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│ candidates  │    │   resumes   │    │resume_skills│
├─────────────┤    ├─────────────┤    ├─────────────┤
│ id (PK)     │───▶│ id (PK)     │    │ resume_id   │
│ user_id (FK)│    │candidate_id │    │ skill_name  │
│ summary     │    │ file_path   │    │ proficiency │
│ location    │    │ file_name   │    │ years_exp   │
│ linkedin    │    │ parsed_text │    └─────────────┘
│ github      │    │ skills_json │
│ website     │    │ created_at  │
└─────────────┘    │ updated_at  │
                   └─────────────┘

┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│ education   │    │ experience  │    │ recruiters  │
├─────────────┤    ├─────────────┤    ├─────────────┤
│ id (PK)     │    │ id (PK)     │    │ id (PK)     │
│candidate_id │    │candidate_id │    │ user_id (FK)│
│ degree      │    │ company     │    │ company     │
│ field       │    │ position    │    │ department  │
│ institution │    │ description │    │ phone       │
│ start_date  │    │ start_date  │    └─────────────┘
│ end_date    │    │ end_date    │
│ gpa         │    │ is_current  │
└─────────────┘    └─────────────┘

┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│    jobs     │    │ job_skills  │    │applications │
├─────────────┤    ├─────────────┤    ├─────────────┤
│ id (PK)     │───▶│ job_id (FK) │    │ id (PK)     │
│recruiter_id │    │ skill_name  │    │candidate_id │
│ title       │    │ required    │    │ job_id (FK) │
│ description │    │ min_years   │    │ status      │
│ requirements│    │ weight      │    │ applied_at  │
│ location    │    └─────────────┘    │ updated_at  │
│ salary_min  │                       │ cover_letter│
│ salary_max  │                       │ match_score │
│ job_type    │                       └─────────────┘
│ remote      │
│ status      │
│ created_at  │
│ updated_at  │
│ expires_at  │
└─────────────┘

┌─────────────┐
│match_scores │
├─────────────┤
│ id (PK)     │
│candidate_id │
│ job_id (FK) │
│ total_score │
│ skill_score │
│ exp_score   │
│ edu_score   │
│ details_json│
│ calculated_at│
└─────────────┘
```

## Table Definitions

### Core Tables

**users**
- Primary user table for all user types
- Stores authentication and basic profile info

**roles**
- CANDIDATE, RECRUITER, ADMIN roles
- Supports RBAC

**user_roles**
- Many-to-many relationship between users and roles

### Candidate-specific Tables

**candidates**
- Extended profile for candidates
- Links to user table

**resumes**
- Stores resume files and parsed content
- Uses JSONB for flexible skill storage

**resume_skills**
- Normalized skills extracted from resume
- Includes proficiency and experience levels

**education**
- Academic background

**experience**
- Work history

### Recruiter-specific Tables

**recruiters**
- Extended profile for recruiters

**jobs**
- Job postings with requirements
- Supports salary ranges, location, remote work

**job_skills**
- Required skills for jobs
- Includes minimum experience and weight for matching

### Application & Matching Tables

**applications**
- Job applications with status tracking
- Stores calculated match scores

**match_scores**
- Detailed matching scores between candidates and jobs
- Uses JSONB for flexible scoring details