package com.jobboard.dto.candidate;

import jakarta.validation.constraints.Size;

public class CandidateProfileRequest {

    @Size(max = 2000, message = "Summary must not exceed 2000 characters")
    private String summary;

    @Size(max = 255, message = "Location must not exceed 255 characters")
    private String location;

    @Size(max = 255, message = "LinkedIn URL must not exceed 255 characters")
    private String linkedin;

    @Size(max = 255, message = "GitHub URL must not exceed 255 characters")
    private String github;

    @Size(max = 255, message = "Website URL must not exceed 255 characters")
    private String website;

    // Constructors
    public CandidateProfileRequest() {
    }

    // Getters and Setters
    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLinkedin() {
        return linkedin;
    }

    public void setLinkedin(String linkedin) {
        this.linkedin = linkedin;
    }

    public String getGithub() {
        return github;
    }

    public void setGithub(String github) {
        this.github = github;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }
}