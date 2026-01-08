package com.jobboard.service;

import com.jobboard.domain.*;
import com.jobboard.repository.MatchScoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchingServiceTest {

    @Mock
    private MatchScoreRepository matchScoreRepository;

    @InjectMocks
    private MatchingService matchingService;

    private Candidate testCandidate;
    private Job testJob;
    private Resume testResume;

    @BeforeEach
    void setUp() {
        // Create test candidate
        testCandidate = new Candidate();
        testCandidate.setId(1L);

        // Create test resume with skills
        testResume = new Resume();
        testResume.setId(1L);
        testResume.setCandidate(testCandidate);

        List<ResumeSkill> resumeSkills = new ArrayList<>();
        resumeSkills.add(createResumeSkill("java", 5, ResumeSkill.SkillProficiency.ADVANCED));
        resumeSkills.add(createResumeSkill("spring boot", 3, ResumeSkill.SkillProficiency.INTERMEDIATE));
        resumeSkills.add(createResumeSkill("mysql", 4, ResumeSkill.SkillProficiency.ADVANCED));
        testResume.setResumeSkills(resumeSkills);

        testCandidate.setResumes(List.of(testResume));

        // Create test experience
        List<Experience> experiences = new ArrayList<>();
        Experience exp1 = new Experience();
        exp1.setStartDate(LocalDate.of(2020, 1, 1));
        exp1.setEndDate(LocalDate.of(2023, 1, 1));
        experiences.add(exp1);

        Experience exp2 = new Experience();
        exp2.setStartDate(LocalDate.of(2018, 6, 1));
        exp2.setEndDate(LocalDate.of(2020, 1, 1));
        experiences.add(exp2);

        testCandidate.setExperiences(experiences);

        // Create test education
        List<Education> educations = new ArrayList<>();
        Education education = new Education();
        education.setDegree("Bachelor of Science");
        education.setField("Computer Science");
        educations.add(education);
        testCandidate.setEducations(educations);

        // Create test job
        testJob = new Job();
        testJob.setId(1L);
        testJob.setTitle("Senior Java Developer");
        testJob.setDescription("Looking for a senior Java developer with Spring Boot experience");
        testJob.setRequirements("Bachelor's degree required. 3+ years of Java experience.");

        List<JobSkill> jobSkills = new ArrayList<>();
        jobSkills.add(createJobSkill("java", true, 3, BigDecimal.valueOf(0.4)));
        jobSkills.add(createJobSkill("spring boot", true, 2, BigDecimal.valueOf(0.3)));
        jobSkills.add(createJobSkill("mysql", false, 2, BigDecimal.valueOf(0.2)));
        jobSkills.add(createJobSkill("docker", false, 1, BigDecimal.valueOf(0.1)));
        testJob.setJobSkills(jobSkills);
    }

    @Test
    void shouldCalculateHighMatchScore() {
        // Given
        MatchScore expectedMatchScore = new MatchScore(testCandidate, testJob);
        when(matchScoreRepository.save(any(MatchScore.class))).thenReturn(expectedMatchScore);

        // When
        MatchScore result = matchingService.calculateMatchScore(testCandidate, testJob);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalScore()).isNotNull();
        assertThat(result.getSkillScore()).isNotNull();
        assertThat(result.getExpScore()).isNotNull();
        assertThat(result.getEduScore()).isNotNull();

        // Should have high skill score (candidate has all required skills with good
        // experience)
        assertThat(result.getSkillScore().doubleValue()).isGreaterThan(80.0);

        // Should have good experience score (5 years total experience)
        assertThat(result.getExpScore().doubleValue()).isGreaterThan(80.0);

        // Should have perfect education score (has bachelor's degree)
        assertThat(result.getEduScore().doubleValue()).isEqualTo(100.0);
    }

    @Test
    void shouldCalculateLowerScoreForMissingRequiredSkills() {
        // Given - Remove required skills from candidate
        testResume.getResumeSkills().clear();
        testResume.getResumeSkills().add(createResumeSkill("python", 3, ResumeSkill.SkillProficiency.INTERMEDIATE));

        MatchScore expectedMatchScore = new MatchScore(testCandidate, testJob);
        when(matchScoreRepository.save(any(MatchScore.class))).thenReturn(expectedMatchScore);

        // When
        MatchScore result = matchingService.calculateMatchScore(testCandidate, testJob);

        // Then
        assertThat(result.getSkillScore().doubleValue()).isLessThan(50.0);
    }

    @Test
    void shouldCalculatePerfectScoreForJobWithoutSkillRequirements() {
        // Given - Job with no skill requirements
        testJob.getJobSkills().clear();

        MatchScore expectedMatchScore = new MatchScore(testCandidate, testJob);
        when(matchScoreRepository.save(any(MatchScore.class))).thenReturn(expectedMatchScore);

        // When
        MatchScore result = matchingService.calculateMatchScore(testCandidate, testJob);

        // Then
        assertThat(result.getSkillScore().doubleValue()).isEqualTo(100.0);
    }

    @Test
    void shouldCalculateZeroSkillScoreForCandidateWithoutSkills() {
        // Given - Candidate with no skills
        testCandidate.getResumes().clear();

        MatchScore expectedMatchScore = new MatchScore(testCandidate, testJob);
        when(matchScoreRepository.save(any(MatchScore.class))).thenReturn(expectedMatchScore);

        // When
        MatchScore result = matchingService.calculateMatchScore(testCandidate, testJob);

        // Then
        assertThat(result.getSkillScore().doubleValue()).isEqualTo(0.0);
    }

    @Test
    void shouldCalculateExperienceScoreBasedOnJobDescription() {
        // Given - Job requiring senior level experience
        testJob.setDescription("Senior level position requiring 5+ years of experience");
        testJob.setRequirements("Senior developer with extensive experience");

        MatchScore expectedMatchScore = new MatchScore(testCandidate, testJob);
        when(matchScoreRepository.save(any(MatchScore.class))).thenReturn(expectedMatchScore);

        // When
        MatchScore result = matchingService.calculateMatchScore(testCandidate, testJob);

        // Then
        // Candidate has 5 years experience, job requires senior (5 years)
        assertThat(result.getExpScore().doubleValue()).isEqualTo(100.0);
    }

    @Test
    void shouldCalculateLowerExperienceScoreForJuniorCandidate() {
        // Given - Candidate with less experience
        testCandidate.getExperiences().clear();
        Experience shortExp = new Experience();
        shortExp.setStartDate(LocalDate.of(2023, 1, 1));
        shortExp.setEndDate(LocalDate.of(2024, 1, 1));
        testCandidate.setExperiences(List.of(shortExp));

        MatchScore expectedMatchScore = new MatchScore(testCandidate, testJob);
        when(matchScoreRepository.save(any(MatchScore.class))).thenReturn(expectedMatchScore);

        // When
        MatchScore result = matchingService.calculateMatchScore(testCandidate, testJob);

        // Then
        assertThat(result.getExpScore().doubleValue()).isLessThan(100.0);
        assertThat(result.getExpScore().doubleValue()).isGreaterThan(20.0);
    }

    @Test
    void shouldCalculateEducationScoreBasedOnRequirements() {
        // Given - Job requiring Master's degree
        testJob.setRequirements("Master's degree in Computer Science required");

        // Candidate has Bachelor's degree
        MatchScore expectedMatchScore = new MatchScore(testCandidate, testJob);
        when(matchScoreRepository.save(any(MatchScore.class))).thenReturn(expectedMatchScore);

        // When
        MatchScore result = matchingService.calculateMatchScore(testCandidate, testJob);

        // Then
        // Should have lower education score (Bachelor's vs Master's requirement)
        assertThat(result.getEduScore().doubleValue()).isLessThan(100.0);
        assertThat(result.getEduScore().doubleValue()).isGreaterThan(30.0);
    }

    @Test
    void shouldCalculatePerfectEducationScoreWhenNoRequirement() {
        // Given - Job with no education requirement
        testJob.setRequirements("Experience in Java development");

        MatchScore expectedMatchScore = new MatchScore(testCandidate, testJob);
        when(matchScoreRepository.save(any(MatchScore.class))).thenReturn(expectedMatchScore);

        // When
        MatchScore result = matchingService.calculateMatchScore(testCandidate, testJob);

        // Then
        assertThat(result.getEduScore().doubleValue()).isEqualTo(100.0);
    }

    @Test
    void shouldIncludeDetailedScoringInformation() {
        // Given
        MatchScore expectedMatchScore = new MatchScore(testCandidate, testJob);
        when(matchScoreRepository.save(any(MatchScore.class))).thenReturn(expectedMatchScore);

        // When
        MatchScore result = matchingService.calculateMatchScore(testCandidate, testJob);

        // Then
        assertThat(result.getDetailsJson()).isNotNull();
        assertThat(result.getDetailsJson()).containsKey("skillBreakdown");
        assertThat(result.getDetailsJson()).containsKey("experienceYears");
        assertThat(result.getDetailsJson()).containsKey("educationLevel");
        assertThat(result.getDetailsJson()).containsKey("scoringWeights");
    }

    @Test
    void shouldApplyCorrectWeightingToTotalScore() {
        // Given
        MatchScore expectedMatchScore = new MatchScore(testCandidate, testJob);
        when(matchScoreRepository.save(any(MatchScore.class))).thenReturn(expectedMatchScore);

        // When
        MatchScore result = matchingService.calculateMatchScore(testCandidate, testJob);

        // Then
        double expectedTotal = (result.getSkillScore().doubleValue() * 0.5) +
                (result.getExpScore().doubleValue() * 0.3) +
                (result.getEduScore().doubleValue() * 0.2);

        assertThat(result.getTotalScore().doubleValue()).isCloseTo(expectedTotal,
                org.assertj.core.data.Offset.offset(0.01));
    }

    private ResumeSkill createResumeSkill(String skillName, Integer yearsExp,
            ResumeSkill.SkillProficiency proficiency) {
        ResumeSkill skill = new ResumeSkill();
        skill.setResume(testResume);
        skill.setSkillName(skillName);
        skill.setYearsExp(yearsExp);
        skill.setProficiency(proficiency);
        return skill;
    }

    private JobSkill createJobSkill(String skillName, boolean required, Integer minYears, BigDecimal weight) {
        JobSkill jobSkill = new JobSkill();
        jobSkill.setJob(testJob);
        jobSkill.setSkillName(skillName);
        jobSkill.setRequired(required);
        jobSkill.setMinYears(minYears);
        jobSkill.setWeight(weight);
        return jobSkill;
    }
}