package com.jobboard.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "job_skills")
public class JobSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Column(name = "skill_name", nullable = false)
    private String skillName;

    private Boolean required = false;

    @Column(name = "min_years")
    private Integer minYears;

    @Column(precision = 3, scale = 2)
    private BigDecimal weight = BigDecimal.valueOf(1.0);

    // Constructors
    public JobSkill() {
    }

    public JobSkill(Job job, String skillName, Boolean required) {
        this.job = job;
        this.skillName = skillName;
        this.required = required;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
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