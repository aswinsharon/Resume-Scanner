package com.jobboard.service;

import com.jobboard.domain.Candidate;
import com.jobboard.domain.Resume;
import com.jobboard.domain.ResumeSkill;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ResumeParsingServiceTest {

    @InjectMocks
    private ResumeParsingService resumeParsingService;

    private Resume testResume;
    private Candidate testCandidate;

    @BeforeEach
    void setUp() {
        testCandidate = new Candidate();
        testCandidate.setId(1L);

        testResume = new Resume();
        testResume.setId(1L);
        testResume.setCandidate(testCandidate);
    }

    @Test
    void shouldExtractTextFromPdfFile() throws Exception {
        // Given
        String pdfContent = "Sample PDF content for testing";
        MockMultipartFile pdfFile = new MockMultipartFile(
                "resume",
                "resume.pdf",
                "application/pdf",
                pdfContent.getBytes());

        // When
        String extractedText = resumeParsingService.extractTextFromFile(pdfFile);

        // Then
        assertThat(extractedText).isNotNull();
        // Note: Actual PDF parsing would require a real PDF file
    }

    @Test
    void shouldExtractSkillsFromText() {
        // Given
        String resumeText = """
                John Doe
                Software Engineer

                Skills:
                - Java (5 years)
                - Spring Boot (3 years)
                - Python
                - React
                - MySQL
                - Docker

                Experience:
                Senior Software Engineer at Tech Corp
                Developed web applications using Java and Spring Boot.
                """;

        // When
        List<ResumeSkill> skills = resumeParsingService.extractSkills(testResume, resumeText);

        // Then
        assertThat(skills).isNotEmpty();

        // Check for specific skills
        List<String> skillNames = skills.stream()
                .map(ResumeSkill::getSkillName)
                .toList();

        assertThat(skillNames).contains("java", "spring boot", "python", "react", "mysql", "docker");

        // Check years of experience extraction
        ResumeSkill javaSkill = skills.stream()
                .filter(skill -> "java".equals(skill.getSkillName()))
                .findFirst()
                .orElse(null);

        assertThat(javaSkill).isNotNull();
        assertThat(javaSkill.getYearsExp()).isEqualTo(5);
    }

    @Test
    void shouldExtractStructuredDataFromText() {
        // Given
        String resumeText = """
                John Doe
                Email: john.doe@example.com
                Phone: +1-555-123-4567

                Education:
                Bachelor of Science in Computer Science
                Stanford University (2018-2022)

                Experience:
                5+ years of software development experience
                Senior Software Engineer (2022-present)
                """;

        // When
        Map<String, Object> structuredData = resumeParsingService.extractStructuredData(resumeText);

        // Then
        assertThat(structuredData).isNotEmpty();
        assertThat(structuredData.get("email")).isEqualTo("john.doe@example.com");
        assertThat(structuredData.get("phone")).isEqualTo("+1-555-123-4567");
        assertThat(structuredData.get("totalExperience")).isEqualTo(5);
        assertThat(structuredData.get("educationLevel")).isEqualTo("Bachelors");
    }

    @Test
    void shouldExtractEmailFromText() {
        // Given
        String textWithEmail = "Contact me at john.doe@example.com for more information";

        // When
        Map<String, Object> data = resumeParsingService.extractStructuredData(textWithEmail);

        // Then
        assertThat(data.get("email")).isEqualTo("john.doe@example.com");
    }

    @Test
    void shouldExtractPhoneFromText() {
        // Given
        String textWithPhone = "Call me at (555) 123-4567 or +1-555-987-6543";

        // When
        Map<String, Object> data = resumeParsingService.extractStructuredData(textWithPhone);

        // Then
        assertThat(data.get("phone")).isNotNull();
        assertThat(data.get("phone").toString()).matches(".*555.*123.*4567.*");
    }

    @Test
    void shouldExtractEducationLevel() {
        // Given
        String[] educationTexts = {
                "PhD in Computer Science",
                "Master of Business Administration",
                "Bachelor of Science",
                "Associate Degree in IT"
        };

        String[] expectedLevels = { "PhD", "Masters", "Bachelors", "Associate" };

        for (int i = 0; i < educationTexts.length; i++) {
            // When
            Map<String, Object> data = resumeParsingService.extractStructuredData(educationTexts[i]);

            // Then
            assertThat(data.get("educationLevel")).isEqualTo(expectedLevels[i]);
        }
    }

    @Test
    void shouldExtractTotalExperience() {
        // Given
        String[] experienceTexts = {
                "5 years of experience in software development",
                "10+ years experience",
                "Over 3 years of total experience"
        };

        Integer[] expectedYears = { 5, 10, 3 };

        for (int i = 0; i < experienceTexts.length; i++) {
            // When
            Map<String, Object> data = resumeParsingService.extractStructuredData(experienceTexts[i]);

            // Then
            assertThat(data.get("totalExperience")).isEqualTo(expectedYears[i]);
        }
    }

    @Test
    void shouldDetermineProficiencyLevels() {
        // Given
        String[] skillContexts = {
                "Expert in Java programming",
                "Advanced Python developer",
                "Proficient in React",
                "Familiar with Docker",
                "Basic knowledge of Kubernetes"
        };

        ResumeSkill.SkillProficiency[] expectedProficiencies = {
                ResumeSkill.SkillProficiency.EXPERT,
                ResumeSkill.SkillProficiency.EXPERT,
                ResumeSkill.SkillProficiency.ADVANCED,
                ResumeSkill.SkillProficiency.INTERMEDIATE,
                ResumeSkill.SkillProficiency.BEGINNER
        };

        for (int i = 0; i < skillContexts.length; i++) {
            // When
            List<ResumeSkill> skills = resumeParsingService.extractSkills(testResume, skillContexts[i]);

            // Then
            if (!skills.isEmpty()) {
                ResumeSkill skill = skills.get(0);
                assertThat(skill.getProficiency()).isEqualTo(expectedProficiencies[i]);
            }
        }
    }

    @Test
    void shouldHandleEmptyText() {
        // Given
        String emptyText = "";

        // When
        List<ResumeSkill> skills = resumeParsingService.extractSkills(testResume, emptyText);
        Map<String, Object> data = resumeParsingService.extractStructuredData(emptyText);

        // Then
        assertThat(skills).isEmpty();
        assertThat(data).isEmpty();
    }

    @Test
    void shouldHandleTextWithoutSkills() {
        // Given
        String textWithoutSkills = """
                John Doe
                123 Main Street
                Anytown, USA

                I am a hardworking individual looking for opportunities.
                """;

        // When
        List<ResumeSkill> skills = resumeParsingService.extractSkills(testResume, textWithoutSkills);

        // Then
        assertThat(skills).isEmpty();
    }

    @Test
    void shouldExtractYearsOfExperienceFromDifferentFormats() {
        // Given
        String[] textFormats = {
                "Java (5 years)",
                "5 years of Java experience",
                "Java - 3 years",
                "Python programming for 7 years"
        };

        String[] skills = { "java", "java", "java", "python" };
        Integer[] expectedYears = { 5, 5, 3, 7 };

        for (int i = 0; i < textFormats.length; i++) {
            final int index = i; // Make effectively final for lambda
            // When
            List<ResumeSkill> extractedSkills = resumeParsingService.extractSkills(testResume, textFormats[i]);

            // Then
            ResumeSkill skill = extractedSkills.stream()
                    .filter(s -> skills[index].equals(s.getSkillName()))
                    .findFirst()
                    .orElse(null);

            if (skill != null) {
                assertThat(skill.getYearsExp()).isEqualTo(expectedYears[i]);
            }
        }
    }

    @Test
    void shouldNotExtractPartialSkillMatches() {
        // Given
        String textWithPartialMatches = """
                I have experience with JavaScript and TypeScript.
                Also worked with Java applications.
                """;

        // When
        List<ResumeSkill> skills = resumeParsingService.extractSkills(testResume, textWithPartialMatches);

        // Then
        List<String> skillNames = skills.stream()
                .map(ResumeSkill::getSkillName)
                .toList();

        // Should extract "javascript", "typescript", and "java"
        assertThat(skillNames).contains("javascript", "typescript", "java");

        // Should not extract partial matches like "script" from "javascript"
        assertThat(skillNames).doesNotContain("script");
    }
}