package com.jobboard.util;

import com.jobboard.domain.*;
import com.jobboard.dto.auth.LoginRequest;
import com.jobboard.dto.auth.RegisterRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

/**
 * Utility class for building test data objects
 */
public class TestDataBuilder {

    public static User createTestUser(String email, String firstName, String lastName) {
        User user = new User(email, "encodedPassword", firstName, lastName);
        user.setId(1L);
        user.setPhone("+1234567890");
        user.setIsActive(true);
        return user;
    }

    public static Role createTestRole(Role.RoleName roleName) {
        Role role = new Role(roleName);
        role.setId(1L);
        role.setDescription(roleName.name() + " role");
        return role;
    }

    public static Candidate createTestCandidate(User user) {
        Candidate candidate = new Candidate(user);
        candidate.setId(1L);
        candidate.setSummary("Experienced software developer");
        candidate.setLocation("San Francisco, CA");
        candidate.setLinkedin("linkedin.com/in/johndoe");
        candidate.setGithub("github.com/johndoe");
        return candidate;
    }

    public static Recruiter createTestRecruiter(User user) {
        Recruiter recruiter = new Recruiter(user, "Tech Corp");
        recruiter.setId(1L);
        recruiter.setDepartment("Engineering");
        recruiter.setPhone("+1987654321");
        return recruiter;
    }

    public static Job createTestJob(Recruiter recruiter) {
        Job job = new Job(recruiter, "Senior Java Developer", "Looking for experienced Java developer");
        job.setId(1L);
        job.setRequirements("5+ years of Java experience");
        job.setLocation("San Francisco, CA");
        job.setSalaryMin(BigDecimal.valueOf(120000));
        job.setSalaryMax(BigDecimal.valueOf(180000));
        job.setJobType(Job.JobType.FULL_TIME);
        job.setRemote(true);
        job.setStatus(Job.JobStatus.ACTIVE);
        return job;
    }

    public static Resume createTestResume(Candidate candidate) {
        Resume resume = new Resume(candidate, "/uploads/resume.pdf", "resume.pdf");
        resume.setId(1L);
        resume.setParsedText("John Doe Software Engineer with 5 years of Java experience");
        return resume;
    }

    public static ResumeSkill createTestResumeSkill(Resume resume, String skillName, Integer yearsExp) {
        ResumeSkill skill = new ResumeSkill(resume, skillName);
        skill.setId(1L);
        skill.setYearsExp(yearsExp);
        skill.setProficiency(ResumeSkill.SkillProficiency.ADVANCED);
        return skill;
    }

    public static JobSkill createTestJobSkill(Job job, String skillName, boolean required, Integer minYears) {
        JobSkill jobSkill = new JobSkill(job, skillName, required);
        jobSkill.setId(1L);
        jobSkill.setMinYears(minYears);
        jobSkill.setWeight(BigDecimal.valueOf(1.0));
        return jobSkill;
    }

    public static Education createTestEducation(Candidate candidate) {
        Education education = new Education(candidate, "Bachelor of Science", "Computer Science",
                "Stanford University");
        education.setId(1L);
        education.setStartDate(LocalDate.of(2018, 9, 1));
        education.setEndDate(LocalDate.of(2022, 6, 1));
        education.setGpa(BigDecimal.valueOf(3.8));
        return education;
    }

    public static Experience createTestExperience(Candidate candidate) {
        Experience experience = new Experience(candidate, "Tech Corp", "Software Engineer");
        experience.setId(1L);
        experience.setDescription("Developed web applications using Java and Spring Boot");
        experience.setStartDate(LocalDate.of(2022, 7, 1));
        experience.setEndDate(LocalDate.of(2024, 1, 1));
        experience.setIsCurrent(false);
        return experience;
    }

    public static Application createTestApplication(Candidate candidate, Job job) {
        Application application = new Application(candidate, job);
        application.setId(1L);
        application.setCoverLetter("I am interested in this position");
        application.setMatchScore(BigDecimal.valueOf(85.5));
        application.setStatus(Application.ApplicationStatus.APPLIED);
        return application;
    }

    public static MatchScore createTestMatchScore(Candidate candidate, Job job) {
        MatchScore matchScore = new MatchScore(candidate, job);
        matchScore.setId(1L);
        matchScore.setTotalScore(BigDecimal.valueOf(85.5));
        matchScore.setSkillScore(BigDecimal.valueOf(90.0));
        matchScore.setExpScore(BigDecimal.valueOf(80.0));
        matchScore.setEduScore(BigDecimal.valueOf(85.0));
        return matchScore;
    }

    public static RegisterRequest createTestRegisterRequest(String email, String role) {
        RegisterRequest request = new RegisterRequest();
        request.setEmail(email);
        request.setPassword("password123");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setPhone("+1234567890");
        request.setRole(role);

        if ("RECRUITER".equals(role)) {
            request.setCompany("Tech Corp");
            request.setDepartment("Engineering");
        }

        return request;
    }

    public static LoginRequest createTestLoginRequest(String email) {
        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword("password123");
        return request;
    }

    public static User createUserWithRoles(String email, Role.RoleName... roleNames) {
        User user = createTestUser(email, "John", "Doe");

        for (Role.RoleName roleName : roleNames) {
            Role role = createTestRole(roleName);
            user.addRole(role);
        }

        return user;
    }
}