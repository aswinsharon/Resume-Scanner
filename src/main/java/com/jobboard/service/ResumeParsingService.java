package com.jobboard.service;

import com.jobboard.domain.Resume;
import com.jobboard.domain.ResumeSkill;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ResumeParsingService {

    private final Tika tika = new Tika();

    // Common programming skills and technologies
    private static final Set<String> TECH_SKILLS = Set.of(
            "java", "python", "javascript", "typescript", "c++", "c#", "php", "ruby", "go", "rust",
            "spring", "spring boot", "react", "angular", "vue", "node.js", "express", "django", "flask",
            "mysql", "postgresql", "mongodb", "redis", "elasticsearch", "docker", "kubernetes",
            "aws", "azure", "gcp", "jenkins", "git", "linux", "html", "css", "sql", "nosql",
            "microservices", "rest", "graphql", "junit", "testing", "agile", "scrum", "devops");

    public String extractTextFromFile(MultipartFile file) throws IOException, TikaException {
        return tika.parseToString(file.getInputStream());
    }

    public List<ResumeSkill> extractSkills(Resume resume, String text) {
        List<ResumeSkill> skills = new ArrayList<>();
        String lowerText = text.toLowerCase();

        for (String skill : TECH_SKILLS) {
            if (containsSkill(lowerText, skill)) {
                ResumeSkill resumeSkill = new ResumeSkill(resume, skill);
                resumeSkill.setYearsExp(extractYearsOfExperience(text, skill));
                resumeSkill.setProficiency(determineProficiency(text, skill));
                skills.add(resumeSkill);
            }
        }

        return skills;
    }

    public Map<String, Object> extractStructuredData(String text) {
        Map<String, Object> data = new HashMap<>();

        // Extract email
        String email = extractEmail(text);
        if (email != null) {
            data.put("email", email);
        }

        // Extract phone
        String phone = extractPhone(text);
        if (phone != null) {
            data.put("phone", phone);
        }

        // Extract years of experience
        Integer totalExperience = extractTotalExperience(text);
        if (totalExperience != null) {
            data.put("totalExperience", totalExperience);
        }

        // Extract education level
        String educationLevel = extractEducationLevel(text);
        if (educationLevel != null) {
            data.put("educationLevel", educationLevel);
        }

        return data;
    }

    private boolean containsSkill(String text, String skill) {
        // Use word boundaries to avoid partial matches
        Pattern pattern = Pattern.compile("\\b" + Pattern.quote(skill) + "\\b", Pattern.CASE_INSENSITIVE);
        return pattern.matcher(text).find();
    }

    private Integer extractYearsOfExperience(String text, String skill) {
        // Look for patterns like "5 years of Java experience" or "Java (3 years)"
        String[] patterns = {
                "\\b(\\d+)\\s+years?\\s+(?:of\\s+)?" + Pattern.quote(skill),
                Pattern.quote(skill) + "\\s*\\(\\s*(\\d+)\\s+years?\\)",
                Pattern.quote(skill) + "\\s*-\\s*(\\d+)\\s+years?"
        };

        for (String patternStr : patterns) {
            Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1));
            }
        }

        return null;
    }

    private ResumeSkill.SkillProficiency determineProficiency(String text, String skill) {
        String lowerText = text.toLowerCase();
        String skillContext = extractSkillContext(lowerText, skill);

        if (skillContext.contains("expert") || skillContext.contains("advanced") ||
                skillContext.contains("senior") || skillContext.contains("lead")) {
            return ResumeSkill.SkillProficiency.EXPERT;
        } else if (skillContext.contains("proficient") || skillContext.contains("experienced")) {
            return ResumeSkill.SkillProficiency.ADVANCED;
        } else if (skillContext.contains("intermediate") || skillContext.contains("familiar")) {
            return ResumeSkill.SkillProficiency.INTERMEDIATE;
        } else if (skillContext.contains("beginner") || skillContext.contains("basic")) {
            return ResumeSkill.SkillProficiency.BEGINNER;
        }

        return ResumeSkill.SkillProficiency.INTERMEDIATE; // Default
    }

    private String extractSkillContext(String text, String skill) {
        int skillIndex = text.indexOf(skill.toLowerCase());
        if (skillIndex == -1)
            return "";

        int start = Math.max(0, skillIndex - 50);
        int end = Math.min(text.length(), skillIndex + skill.length() + 50);

        return text.substring(start, end);
    }

    private String extractEmail(String text) {
        Pattern pattern = Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    private String extractPhone(String text) {
        Pattern pattern = Pattern.compile("\\b(?:\\+?1[-.]?)?\\(?([0-9]{3})\\)?[-.]?([0-9]{3})[-.]?([0-9]{4})\\b");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    private Integer extractTotalExperience(String text) {
        String[] patterns = {
                "(\\d+)\\s+years?\\s+(?:of\\s+)?(?:total\\s+)?experience",
                "(\\d+)\\+\\s+years?\\s+experience",
                "over\\s+(\\d+)\\s+years?\\s+experience"
        };

        for (String patternStr : patterns) {
            Pattern pattern = Pattern.compile(patternStr, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1));
            }
        }

        return null;
    }

    private String extractEducationLevel(String text) {
        String lowerText = text.toLowerCase();

        if (lowerText.contains("phd") || lowerText.contains("ph.d") || lowerText.contains("doctorate")) {
            return "PhD";
        } else if (lowerText.contains("master") || lowerText.contains("mba") || lowerText.contains("ms")
                || lowerText.contains("ma")) {
            return "Masters";
        } else if (lowerText.contains("bachelor") || lowerText.contains("bs") || lowerText.contains("ba")
                || lowerText.contains("bsc")) {
            return "Bachelors";
        } else if (lowerText.contains("associate") || lowerText.contains("diploma")) {
            return "Associate";
        }

        return null;
    }
}