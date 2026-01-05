package com.jobboard.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "resume_skills")
public class ResumeSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @Column(name = "skill_name", nullable = false)
    private String skillName;

    @Enumerated(EnumType.STRING)
    private SkillProficiency proficiency;

    @Column(name = "years_exp")
    private Integer yearsExp;

    // Constructors
    public ResumeSkill() {
    }

    public ResumeSkill(Resume resume, String skillName) {
        this.resume = resume;
        this.skillName = skillName;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Resume getResume() {
        return resume;
    }

    public void setResume(Resume resume) {
        this.resume = resume;
    }

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public SkillProficiency getProficiency() {
        return proficiency;
    }

    public void setProficiency(SkillProficiency proficiency) {
        this.proficiency = proficiency;
    }

    public Integer getYearsExp() {
        return yearsExp;
    }

    public void setYearsExp(Integer yearsExp) {
        this.yearsExp = yearsExp;
    }

    public enum SkillProficiency {
        BEGINNER, INTERMEDIATE, ADVANCED, EXPERT
    }
}