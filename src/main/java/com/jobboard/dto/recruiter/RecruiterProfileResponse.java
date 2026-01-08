package com.jobboard.dto.recruiter;

import com.jobboard.dto.user.UserResponse;

public class RecruiterProfileResponse {

    private Long id;
    private UserResponse user;
    private String company;
    private String department;
    private String phone;

    // Constructors
    public RecruiterProfileResponse() {
    }

    public RecruiterProfileResponse(Long id, UserResponse user, String company, String department, String phone) {
        this.id = id;
        this.user = user;
        this.company = company;
        this.department = department;
        this.phone = phone;
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