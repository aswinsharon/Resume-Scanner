package com.jobboard.dto.job;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;

public class JobSkillRequest {

    @NotBlank(message = "Skill name is required")
    private String skillName;

    private Boolean required = false;

    @Min(value = 0, message = "Minimum years must be at least 0")
    private Integer minYears;

    @DecimalMin(value = "0.0", message = "Weight must be at least 0.0")
    @DecimalMax(value = "1.0", message = "Weight must not exceed 1.0")
    private BigDecimal weight = BigDecimal.valueOf(1.0);

    // Constructors
    public JobSkillRequest() {
    }

    public JobSkillRequest(String skillName, Boolean required, Integer minYears, BigDecimal weight) {
        this.skillName = skillName;
        this.required = required;
        this.minYears = minYears;
        this.weight = weight;
    }

    // Getters and Setters
    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public Integer getMinYears() {
        return minYears;
    }

    public void setMinYears(Integer minYears) {
        this.minYears = minYears;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }
}