package com.jobboard.dto.candidate;

import com.jobboard.dto.user.UserResponse;

public class CandidateProfileResponse {

    private Long id;
    private UserResponse user;
    private String summary;
    private String location;
    private String linkedin;
    private String github;
    private String website;

    // Constructors
    public CandidateProfileResponse() {
    }

    public CandidateProfileResponse(Long id, UserResponse user, String summary,
            String location, String linkedin, String github, String website) {
        this.id = id;
        this.user = user;
        this.summary = summary;
        this.location = location;
        this.linkedin = linkedin;
        this.github = github;
        this.website = website;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserResponse getUser() {
        return user;
    }

    public void setUser(UserResponse user) {
        this.user = user;
    }

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