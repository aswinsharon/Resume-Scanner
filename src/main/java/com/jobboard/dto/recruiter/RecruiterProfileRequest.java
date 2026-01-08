package com.jobboard.dto.recruiter;

import jakarta.validation.constraints.Size;

public class RecruiterProfileRequest {

    @Size(max = 255, message = "Company name must not exceed 255 characters")
    private String company;

    @Size(max = 255, message = "Department must not exceed 255 characters")
    private String department;

    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;

    // Constructors
    public RecruiterProfileRequest() {
    }

    // Getters and Setters
    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}