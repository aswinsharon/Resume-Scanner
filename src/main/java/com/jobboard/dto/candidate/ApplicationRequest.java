package com.jobboard.dto.candidate;

import jakarta.validation.constraints.Size;

public class ApplicationRequest {

    @Size(max = 2000, message = "Cover letter must not exceed 2000 characters")
    private String coverLetter;

    // Constructors
    public ApplicationRequest() {
    }

    public ApplicationRequest(String coverLetter) {
        this.coverLetter = coverLetter;
    }

    // Getters and Setters
    public String getCoverLetter() {
        return coverLetter;
    }

    public void setCoverLetter(String coverLetter) {
        this.coverLetter = coverLetter;
    }
}