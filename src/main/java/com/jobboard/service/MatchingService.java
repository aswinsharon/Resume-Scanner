package com.jobboard.service;

import com.jobboard.domain.*;
import com.jobboard.repository.MatchScoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MatchingService {

    @Autowired
    private MatchScoreRepository matchScoreRepository;

    // Scoring weights
    private static final double SKILL_WEIGHT = 0.5;
    private static final double EXPERIENCE_WEIGHT = 0.3;
    private static final double EDUCATION_WEIGHT = 0.2;

    public MatchScore calculateMatchScore(Candidate candidate, Job job) {
        // Calculate individual scores
        double skillScore = calculateSkillScore(candidate, job);
        double experienceScore = calculateExperienceScore(candidate, job);
        double educationScore = calculateEducationScore(candidate, job);

        // Calculate weighted total score
        double totalScore = (skillScore * SKILL_WEIGHT) +
                (experienceScore * EXPERIENCE_WEIGHT) +
                (educationScore * EDUCATION_WEIGHT);

        // Create match score entity
        MatchScore matchScore = new MatchScore(candidate, job);
        matchScore.setTotalScore(BigDecimal.valueOf(totalScore).setScale(2, RoundingMode.HALF_UP));
        matchScore.setSkillScore(BigDecimal.valueOf(skillScore).setScale(2, RoundingMode.HALF_UP));
        matchScore.setExpScore(BigDecimal.valueOf(experienceScore).setScale(2, RoundingMode.HALF_UP));
        matchScore.setEduScore(BigDecimal.valueOf(educationScore).setScale(2, RoundingMode.HALF_UP));

        // Store detailed scoring information
        Map<String, Object> details = createScoringDetails(candidate, job, skillScore, experienceScore, educationScore);
        matchScore.setDetailsJson(details);

        return matchScoreRepository.save(matchScore);
    }

    private double calculateSkillScore(Candidate candidate, Job job) {
        List<JobSkill> requiredSkills = job.getJobSkills();
        if (requiredSkills.isEmpty()) {
            return 100.0; // No specific skills required
        }

        // Get candidate's skills from latest resume
        Set<String> candidateSkills = getCandidateSkills(candidate);
        if (candidateSkills.isEmpty()) {
            return 0.0;
        }

        double totalWeightedScore = 0.0;
        double totalWeight = 0.0;
        int matchedRequiredSkills = 0;
        int totalRequiredSkills = 0;

        for (JobSkill jobSkill : requiredSkills) {
            double weight = jobSkill.getWeight() != null ? jobSkill.getWeight().doubleValue() : 1.0;
            totalWeight += weight;

            if (jobSkill.getRequired()) {
                totalRequiredSkills++;
            }

            if (candidateSkills.contains(jobSkill.getSkillName().toLowerCase())) {
                double skillScore = calculateIndividualSkillScore(candidate, jobSkill);
                totalWeightedScore += skillScore * weight;

                if (jobSkill.getRequired()) {
                    matchedRequiredSkills++;
                }
            }
        }

        // Penalty for missing required skills
        double requiredSkillsPenalty = totalRequiredSkills > 0 ? (double) matchedRequiredSkills / totalRequiredSkills
                : 1.0;

        double averageScore = totalWeight > 0 ? totalWeightedScore / totalWeight : 0.0;

        return Math.min(100.0, averageScore * requiredSkillsPenalty);
    }

    private double calculateIndividualSkillScore(Candidate candidate, JobSkill jobSkill) {
        // Get candidate's experience with this skill
        Integer candidateYears = getCandidateSkillExperience(candidate, jobSkill.getSkillName());
        Integer requiredYears = jobSkill.getMinYears() != null ? jobSkill.getMinYears() : 0;

        if (candidateYears == null) {
            return 70.0; // Has skill but no specific experience data
        }

        if (candidateYears >= requiredYears) {
            // Bonus for exceeding requirements
            double bonus = Math.min(30.0, (candidateYears - requiredYears) * 5.0);
            return 100.0 + bonus;
        } else {
            // Partial score based on experience ratio
            return Math.max(50.0, (double) candidateYears / requiredYears * 100.0);
        }
    }

    private double calculateExperienceScore(Candidate candidate, Job job) {
        int totalExperience = calculateTotalExperience(candidate);

        // Estimate required experience from job description or use default
        int estimatedRequiredExperience = estimateRequiredExperience(job);

        if (totalExperience >= estimatedRequiredExperience) {
            return 100.0;
        } else if (totalExperience == 0) {
            return 20.0; // Entry level
        } else {
            return Math.max(20.0, (double) totalExperience / estimatedRequiredExperience * 100.0);
        }
    }

    private double calculateEducationScore(Candidate candidate, Job job) {
        String candidateEducation = getHighestEducationLevel(candidate);
        String requiredEducation = extractRequiredEducation(job);

        if (requiredEducation == null) {
            return 100.0; // No specific education requirement
        }

        return compareEducationLevels(candidateEducation, requiredEducation);
    }

    private Set<String> getCandidateSkills(Candidate candidate) {
        return candidate.getResumes().stream()
                .filter(resume -> resume.getResumeSkills() != null)
                .flatMap(resume -> resume.getResumeSkills().stream())
                .map(skill -> skill.getSkillName().toLowerCase())
                .collect(Collectors.toSet());
    }

    private Integer getCandidateSkillExperience(Candidate candidate, String skillName) {
        return candidate.getResumes().stream()
                .filter(resume -> resume.getResumeSkills() != null)
                .flatMap(resume -> resume.getResumeSkills().stream())
                .filter(skill -> skill.getSkillName().equalsIgnoreCase(skillName))
                .map(ResumeSkill::getYearsExp)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(null);
    }

    private int calculateTotalExperience(Candidate candidate) {
        return candidate.getExperiences().stream()
                .mapToInt(exp -> {
                    LocalDate start = exp.getStartDate();
                    LocalDate end = exp.getEndDate() != null ? exp.getEndDate() : LocalDate.now();
                    return start != null ? Period.between(start, end).getYears() : 0;
                })
                .sum();
    }

    private int estimateRequiredExperience(Job job) {
        String description = (job.getDescription() + " " + job.getRequirements()).toLowerCase();

        if (description.contains("senior") || description.contains("lead")) {
            return 5;
        } else if (description.contains("mid-level") || description.contains("intermediate")) {
            return 3;
        } else if (description.contains("junior") || description.contains("entry")) {
            return 1;
        }

        return 2; // Default
    }

    private String getHighestEducationLevel(Candidate candidate) {
        return candidate.getEducations().stream()
                .map(Education::getDegree)
                .filter(Objects::nonNull)
                .map(String::toLowerCase)
                .max(this::compareEducationStrings)
                .orElse(null);
    }

    private String extractRequiredEducation(Job job) {
        String requirements = (job.getRequirements() != null ? job.getRequirements() : "").toLowerCase();

        if (requirements.contains("phd") || requirements.contains("doctorate")) {
            return "phd";
        } else if (requirements.contains("master") || requirements.contains("mba")) {
            return "masters";
        } else if (requirements.contains("bachelor") || requirements.contains("degree")) {
            return "bachelors";
        }

        return null;
    }

    private double compareEducationLevels(String candidateEd, String requiredEd) {
        if (candidateEd == null) {
            return 50.0; // No education info
        }

        int candidateLevel = getEducationLevel(candidateEd);
        int requiredLevel = getEducationLevel(requiredEd);

        if (candidateLevel >= requiredLevel) {
            return 100.0;
        } else {
            return Math.max(30.0, (double) candidateLevel / requiredLevel * 100.0);
        }
    }

    private int getEducationLevel(String education) {
        education = education.toLowerCase();
        if (education.contains("phd") || education.contains("doctorate"))
            return 4;
        if (education.contains("master") || education.contains("mba"))
            return 3;
        if (education.contains("bachelor"))
            return 2;
        if (education.contains("associate") || education.contains("diploma"))
            return 1;
        return 0;
    }

    private int compareEducationStrings(String ed1, String ed2) {
        return Integer.compare(getEducationLevel(ed1), getEducationLevel(ed2));
    }

    private Map<String, Object> createScoringDetails(Candidate candidate, Job job,
            double skillScore, double experienceScore, double educationScore) {
        Map<String, Object> details = new HashMap<>();

        details.put("skillBreakdown", createSkillBreakdown(candidate, job));
        details.put("experienceYears", calculateTotalExperience(candidate));
        details.put("educationLevel", getHighestEducationLevel(candidate));
        details.put("scoringWeights", Map.of(
                "skills", SKILL_WEIGHT,
                "experience", EXPERIENCE_WEIGHT,
                "education", EDUCATION_WEIGHT));

        return details;
    }

    private Map<String, Object> createSkillBreakdown(Candidate candidate, Job job) {
        Map<String, Object> breakdown = new HashMap<>();
        Set<String> candidateSkills = getCandidateSkills(candidate);

        List<Map<String, Object>> skillMatches = job.getJobSkills().stream()
                .map(jobSkill -> {
                    Map<String, Object> match = new HashMap<>();
                    match.put("skill", jobSkill.getSkillName());
                    match.put("required", jobSkill.getRequired());
                    match.put("minYears", jobSkill.getMinYears());
                    match.put("weight", jobSkill.getWeight());
                    match.put("matched", candidateSkills.contains(jobSkill.getSkillName().toLowerCase()));
                    match.put("candidateYears", getCandidateSkillExperience(candidate, jobSkill.getSkillName()));
                    return match;
                })
                .collect(Collectors.toList());

        breakdown.put("skillMatches", skillMatches);
        breakdown.put("totalRequiredSkills", job.getJobSkills().size());
        breakdown.put("matchedSkills", skillMatches.stream().mapToInt(m -> (Boolean) m.get("matched") ? 1 : 0).sum());

        return breakdown;
    }
}