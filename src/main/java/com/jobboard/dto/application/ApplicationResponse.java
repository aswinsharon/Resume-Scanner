package com.jobboard.dto.application;

import com.jobboard.domain.Application;
import com.jobboard.dto.job.JobResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ApplicationResponse {

    private Long id;
    private JobResponse job;
    private Application.ApplicationStatus status;
    private LocalDateTime appliedAt;
    private LocalDateTime updatedAt;
    private String coverLetter;
    private BigDecimal matchScore;

    // Constructors
    public ApplicationResponse() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public JobResponse getJob() {
        return job;
    }

    public void setJob(JobResponse job) {
        this.job = job;
    }

    public Application.ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(Application.ApplicationStatus status) {
        this.status = status;
    }

    public LocalDateTime getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(LocalDateTime appliedAt) {
        this.appliedAt = appliedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCoverLetter() {
        return coverLetter;
    }

    public void setCoverLetter(String coverLetter) {
        this.coverLetter = coverLetter;
    }

    public BigDecimal getMatchScore() {
        return matchScore;
    }

    public void setMatchScore(BigDecimal matchScore) {
        this.matchScore = matchScore;
    }
}