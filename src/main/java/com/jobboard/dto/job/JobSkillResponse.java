package com.jobboard.dto.job;

import java.math.BigDecimal;

public class JobSkillResponse {

    private Long id;
    private String skillName;
    private Boolean required;
    private Integer minYears;
    private BigDecimal weight;

    // Constructors
    public JobSkillResponse() {
    }

    public JobSkillResponse(Long id, String skillName, Boolean required, Integer minYears, BigDecimal weight) {
        this.id = id;
        this.skillName = skillName;
        this.required = required;
        this.minYears = minYears;
        this.weight = weight;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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