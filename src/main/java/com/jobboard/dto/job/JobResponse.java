package com.jobboard.dto.job;

import com.jobboard.domain.Job;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class JobResponse {

    private Long id;
    private String title;
    private String description;
    private String requirements;
    private String location;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private Job.JobType jobType;
    private Boolean remote;
    private Job.JobStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime expiresAt;
    private String recruiterCompany;
    private List<JobSkillResponse> skills;

    // Constructors
    public JobResponse() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRequirements() {
        return requirements;
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public BigDecimal getSalaryMin() {
        return salaryMin;
    }

    public void setSalaryMin(BigDecimal salaryMin) {
        this.salaryMin = salaryMin;
    }

    public BigDecimal getSalaryMax() {
        return salaryMax;
    }

    public void setSalaryMax(BigDecimal salaryMax) {
        this.salaryMax = salaryMax;
    }

    public Job.JobType getJobType() {
        return jobType;
    }

    public void setJobType(Job.JobType jobType) {
        this.jobType = jobType;
    }

    public Boolean getRemote() {
        return remote;
    }

    public void setRemote(Boolean remote) {
        this.remote = remote;
    }

    public Job.JobStatus getStatus() {
        return status;
    }

    public void setStatus(Job.JobStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getRecruiterCompany() {
        return recruiterCompany;
    }

    public void setRecruiterCompany(String recruiterCompany) {
        this.recruiterCompany = recruiterCompany;
    }

    public List<JobSkillResponse> getSkills() {
        return skills;
    }

    public void setSkills(List<JobSkillResponse> skills) {
        this.skills = skills;
    }
}