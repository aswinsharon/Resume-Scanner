package com.jobboard.dto.admin;

public class PlatformStatsResponse {

    private Long totalUsers;
    private Long totalCandidates;
    private Long totalRecruiters;
    private Long totalJobs;
    private Long activeJobs;
    private Long totalApplications;
    private Long totalResumes;

    // Constructors
    public PlatformStatsResponse() {
    }

    public PlatformStatsResponse(Long totalUsers, Long totalCandidates, Long totalRecruiters,
            Long totalJobs, Long activeJobs, Long totalApplications, Long totalResumes) {
        this.totalUsers = totalUsers;
        this.totalCandidates = totalCandidates;
        this.totalRecruiters = totalRecruiters;
        this.totalJobs = totalJobs;
        this.activeJobs = activeJobs;
        this.totalApplications = totalApplications;
        this.totalResumes = totalResumes;
    }

    // Getters and Setters
    public Long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(Long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public Long getTotalCandidates() {
        return totalCandidates;
    }

    public void setTotalCandidates(Long totalCandidates) {
        this.totalCandidates = totalCandidates;
    }

    public Long getTotalRecruiters() {
        return totalRecruiters;
    }

    public void setTotalRecruiters(Long totalRecruiters) {
        this.totalRecruiters = totalRecruiters;
    }

    public Long getTotalJobs() {
        return totalJobs;
    }

    public void setTotalJobs(Long totalJobs) {
        this.totalJobs = totalJobs;
    }

    public Long getActiveJobs() {
        return activeJobs;
    }

    public void setActiveJobs(Long activeJobs) {
        this.activeJobs = activeJobs;
    }

    public Long getTotalApplications() {
        return totalApplications;
    }

    public void setTotalApplications(Long totalApplications) {
        this.totalApplications = totalApplications;
    }

    public Long getTotalResumes() {
        return totalResumes;
    }

    public void setTotalResumes(Long totalResumes) {
        this.totalResumes = totalResumes;
    }
}