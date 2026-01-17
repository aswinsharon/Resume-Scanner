package com.jobboard.service;

import com.jobboard.domain.*;
import com.jobboard.dto.application.ApplicationResponse;
import com.jobboard.dto.candidate.*;
import com.jobboard.dto.job.JobResponse;
import com.jobboard.dto.user.UserResponse;
import com.jobboard.exception.BadRequestException;
import com.jobboard.exception.ConflictException;
import com.jobboard.exception.FileProcessingException;
import com.jobboard.exception.ResourceNotFoundException;

import com.jobboard.repository.*;

import org.apache.tika.exception.TikaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class CandidateService {

    private static final Logger logger = LoggerFactory.getLogger(CandidateService.class);
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final List<String> ALLOWED_FILE_TYPES = List.of("application/pdf");

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private ResumeRepository resumeRepository;

    @Autowired
    private EducationRepository educationRepository;

    @Autowired
    private ExperienceRepository experienceRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private ResumeParsingService resumeParsingService;

    @Autowired
    private MatchingService matchingService;

    @Value("${file.upload.dir}")
    private String uploadDir;

    public CandidateProfileResponse getProfile(Long userId) {
        try {
            logger.info("Fetching candidate profile for user ID: {}", userId);
            Candidate candidate = candidateRepository.findByUserId(userId)
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Candidate profile not found for user ID: " + userId));

            return convertToCandidateProfileResponse(candidate);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error fetching candidate profile for user ID: {}", userId, e);
            throw new RuntimeException("Failed to fetch candidate profile", e);
        }
    }

    public CandidateProfileResponse updateProfile(Long userId, CandidateProfileRequest request) {
        try {
            logger.info("Updating candidate profile for user ID: {}", userId);

            if (request == null) {
                throw new BadRequestException("Profile update request cannot be null");
            }

            Candidate candidate = candidateRepository.findByUserId(userId)
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Candidate profile not found for user ID: " + userId));

            // Validate input data
            validateProfileRequest(request);

            candidate.setSummary(request.getSummary());
            candidate.setLocation(request.getLocation());
            candidate.setLinkedin(request.getLinkedin());
            candidate.setGithub(request.getGithub());
            candidate.setWebsite(request.getWebsite());

            Candidate savedCandidate = candidateRepository.save(candidate);
            logger.info("Candidate profile updated successfully for user ID: {}", userId);

            return convertToCandidateProfileResponse(savedCandidate);
        } catch (BadRequestException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error updating candidate profile for user ID: {}", userId, e);
            throw new RuntimeException("Failed to update candidate profile", e);
        }
    }

    public Resume uploadResume(Long userId, MultipartFile file) {
        try {
            logger.info("Uploading resume for user ID: {}", userId);

            Candidate candidate = candidateRepository.findByUserId(userId)
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Candidate profile not found for user ID: " + userId));

            // Validate file
            validateResumeFile(file);

            // Create upload directory if it doesn't exist
            Path uploadPath = createUploadDirectory();

            // Generate unique filename and save file
            String uniqueFilename = generateUniqueFilename(file.getOriginalFilename());
            Path filePath = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Parse resume content
            String parsedText = parseResumeContent(file);
            Map<String, Object> structuredData = resumeParsingService.extractStructuredData(parsedText);

            // Create resume record
            Resume resume = new Resume(candidate, filePath.toString(), file.getOriginalFilename());
            resume.setParsedText(parsedText);
            resume.setSkillsJson(structuredData);

            Resume savedResume = resumeRepository.save(resume);

            // Extract and save skills
            List<ResumeSkill> skills = resumeParsingService.extractSkills(savedResume, parsedText);
            savedResume.setResumeSkills(skills);

            logger.info("Resume uploaded successfully for user ID: {}", userId);
            return savedResume;

        } catch (BadRequestException | ResourceNotFoundException | FileProcessingException e) {
            throw e;
        } catch (IOException e) {
            logger.error("IO error during resume upload for user ID: {}", userId, e);
            throw new FileProcessingException("Failed to save resume file", e);
        } catch (Exception e) {
            logger.error("Unexpected error during resume upload for user ID: {}", userId, e);
            throw new RuntimeException("Failed to upload resume", e);
        }
    }

    public Resume getCurrentResume(Long userId) {
        try {
            logger.info("Fetching current resume for user ID: {}", userId);

            Candidate candidate = candidateRepository.findByUserId(userId)
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Candidate profile not found for user ID: " + userId));

            return candidate.getResumes().stream()
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("No resume found for user ID: " + userId));
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error fetching current resume for user ID: {}", userId, e);
            throw new RuntimeException("Failed to fetch current resume", e);
        }
    }

    public Education addEducation(Long userId, EducationRequest request) {
        try {
            logger.info("Adding education for user ID: {}", userId);

            if (request == null) {
                throw new BadRequestException("Education request cannot be null");
            }

            Candidate candidate = candidateRepository.findByUserId(userId)
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Candidate profile not found for user ID: " + userId));

            validateEducationRequest(request);

            Education education = new Education(candidate, request.getDegree(), request.getField(),
                    request.getInstitution());
            education.setStartDate(request.getStartDate());
            education.setEndDate(request.getEndDate());
            education.setGpa(request.getGpa());

            Education savedEducation = educationRepository.save(education);
            logger.info("Education added successfully for user ID: {}", userId);

            return savedEducation;
        } catch (BadRequestException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error adding education for user ID: {}", userId, e);
            throw new RuntimeException("Failed to add education", e);
        }
    }

    public Experience addExperience(Long userId, ExperienceRequest request) {
        try {
            logger.info("Adding experience for user ID: {}", userId);

            if (request == null) {
                throw new BadRequestException("Experience request cannot be null");
            }

            Candidate candidate = candidateRepository.findByUserId(userId)
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Candidate profile not found for user ID: " + userId));

            validateExperienceRequest(request);

            Experience experience = new Experience(candidate, request.getCompany(), request.getPosition());
            experience.setDescription(request.getDescription());
            experience.setStartDate(request.getStartDate());
            experience.setEndDate(request.getEndDate());
            experience.setIsCurrent(request.getIsCurrent());

            Experience savedExperience = experienceRepository.save(experience);
            logger.info("Experience added successfully for user ID: {}", userId);

            return savedExperience;
        } catch (BadRequestException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error adding experience for user ID: {}", userId, e);
            throw new RuntimeException("Failed to add experience", e);
        }
    }

    public List<JobResponse> getRecommendedJobs(Long userId, Pageable pageable) {
        try {
            logger.info("Fetching recommended jobs for user ID: {}", userId);

            Candidate candidate = candidateRepository.findByUserId(userId)
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Candidate profile not found for user ID: " + userId));

            // Get candidate skills
            List<String> candidateSkills = candidate.getResumes().stream()
                    .flatMap(resume -> resume.getResumeSkills().stream())
                    .map(ResumeSkill::getSkillName)
                    .collect(Collectors.toList());

            if (candidateSkills.isEmpty()) {
                logger.info("No skills found for user ID: {}, returning empty recommendations", userId);
                return List.of();
            }

            // Find jobs with matching skills
            List<Job> matchingJobs = jobRepository.findJobsBySkills(candidateSkills);

            return matchingJobs.stream()
                    .map(this::convertToJobResponse)
                    .collect(Collectors.toList());
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error fetching recommended jobs for user ID: {}", userId, e);
            throw new RuntimeException("Failed to fetch recommended jobs", e);
        }
    }

    public Application applyForJob(Long userId, Long jobId, ApplicationRequest request) {
        try {
            logger.info("User {} applying for job {}", userId, jobId);

            if (request == null) {
                throw new BadRequestException("Application request cannot be null");
            }

            Candidate candidate = candidateRepository.findByUserId(userId)
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Candidate profile not found for user ID: " + userId));

            Job job = jobRepository.findById(jobId)
                    .orElseThrow(() -> new ResourceNotFoundException("Job not found with ID: " + jobId));

            // Check if job is still active
            if (job.getStatus() != Job.JobStatus.ACTIVE) {
                throw new BadRequestException("Job is no longer accepting applications");
            }

            // Check if already applied
            if (applicationRepository.existsByCandidateIdAndJobId(candidate.getId(), jobId)) {
                throw new ConflictException("You have already applied for this job");
            }

            // Create application
            Application application = new Application(candidate, job);
            application.setCoverLetter(request.getCoverLetter());

            // Calculate match score
            try {
                MatchScore matchScore = matchingService.calculateMatchScore(candidate, job);
                application.setMatchScore(matchScore.getTotalScore());
            } catch (Exception e) {
                logger.warn("Failed to calculate match score for application, using default", e);
                application.setMatchScore(java.math.BigDecimal.ZERO);
            }

            Application savedApplication = applicationRepository.save(application);
            logger.info("Application submitted successfully for user {} and job {}", userId, jobId);

            return savedApplication;
        } catch (BadRequestException | ResourceNotFoundException | ConflictException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error applying for job {} by user {}", jobId, userId, e);
            throw new RuntimeException("Failed to submit application", e);
        }
    }

    public Page<ApplicationResponse> getApplications(Long userId, Pageable pageable) {
        try {
            logger.info("Fetching applications for user ID: {}", userId);

            Candidate candidate = candidateRepository.findByUserId(userId)
                    .orElseThrow(
                            () -> new ResourceNotFoundException("Candidate profile not found for user ID: " + userId));

            Page<Application> applications = applicationRepository.findByCandidateIdOrderByAppliedAtDesc(
                    candidate.getId(),
                    pageable);

            return applications.map(this::convertToApplicationResponse);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error fetching applications for user ID: {}", userId, e);
            throw new RuntimeException("Failed to fetch applications", e);
        }
    }

    // Validation methods
    private void validateProfileRequest(CandidateProfileRequest request) {
        if (request.getLinkedin() != null && !request.getLinkedin().isEmpty() &&
                !request.getLinkedin().startsWith("https://linkedin.com/") &&
                !request.getLinkedin().startsWith("https://www.linkedin.com/")) {
            throw new BadRequestException("Invalid LinkedIn URL format");
        }

        if (request.getGithub() != null && !request.getGithub().isEmpty() &&
                !request.getGithub().startsWith("https://github.com/")) {
            throw new BadRequestException("Invalid GitHub URL format");
        }

        if (request.getWebsite() != null && !request.getWebsite().isEmpty() &&
                !request.getWebsite().startsWith("http://") &&
                !request.getWebsite().startsWith("https://")) {
            throw new BadRequestException("Invalid website URL format");
        }
    }

    private void validateResumeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Resume file is required");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File size exceeds maximum allowed limit of 10MB");
        }

        String contentType = file.getContentType();
        if (!ALLOWED_FILE_TYPES.contains(contentType)) {
            throw new BadRequestException("Only PDF files are allowed");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".pdf")) {
            throw new BadRequestException("File must have .pdf extension");
        }
    }

    private void validateEducationRequest(EducationRequest request) {
        if (request.getDegree() == null || request.getDegree().trim().isEmpty()) {
            throw new BadRequestException("Degree is required");
        }
        if (request.getField() == null || request.getField().trim().isEmpty()) {
            throw new BadRequestException("Field of study is required");
        }
        if (request.getInstitution() == null || request.getInstitution().trim().isEmpty()) {
            throw new BadRequestException("Institution is required");
        }
        if (request.getStartDate() != null && request.getEndDate() != null &&
                request.getStartDate().isAfter(request.getEndDate())) {
            throw new BadRequestException("Start date cannot be after end date");
        }
    }

    private void validateExperienceRequest(ExperienceRequest request) {
        if (request.getCompany() == null || request.getCompany().trim().isEmpty()) {
            throw new BadRequestException("Company is required");
        }
        if (request.getPosition() == null || request.getPosition().trim().isEmpty()) {
            throw new BadRequestException("Position is required");
        }
        if (request.getStartDate() != null && request.getEndDate() != null &&
                request.getStartDate().isAfter(request.getEndDate())) {
            throw new BadRequestException("Start date cannot be after end date");
        }
        if (Boolean.TRUE.equals(request.getIsCurrent()) && request.getEndDate() != null) {
            throw new BadRequestException("Current position cannot have an end date");
        }
    }

    private Path createUploadDirectory() {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            return uploadPath;
        } catch (IOException e) {
            logger.error("Failed to create upload directory: {}", uploadDir, e);
            throw new FileProcessingException("Failed to create upload directory", e);
        }
    }

    private String generateUniqueFilename(String originalFilename) {
        if (originalFilename == null) {
            throw new BadRequestException("Original filename is required");
        }

        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        return UUID.randomUUID().toString() + fileExtension;
    }

    private String parseResumeContent(MultipartFile file) {
        try {
            return resumeParsingService.extractTextFromFile(file);
        } catch (TikaException e) {
            logger.error("Tika parsing error", e);
            throw new FileProcessingException("Failed to parse resume content", e);
        } catch (IOException e) {
            logger.error("IO error during resume parsing", e);
            throw new FileProcessingException("Failed to read resume file", e);
        } catch (Exception e) {
            logger.error("Unexpected error during resume parsing", e);
            throw new FileProcessingException("Failed to process resume file", e);
        }
    }

    private CandidateProfileResponse convertToCandidateProfileResponse(Candidate candidate) {
        UserResponse userResponse = convertToUserResponse(candidate.getUser());
        return new CandidateProfileResponse(
                candidate.getId(),
                userResponse,
                candidate.getSummary(),
                candidate.getLocation(),
                candidate.getLinkedin(),
                candidate.getGithub(),
                candidate.getWebsite());
    }

    private UserResponse convertToUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.getIsActive(),
                user.getCreatedAt(),
                user.getRoles().stream()
                        .map(role -> role.getName().name())
                        .collect(Collectors.toSet()));
    }

    private JobResponse convertToJobResponse(Job job) {
        JobResponse response = new JobResponse();
        response.setId(job.getId());
        response.setTitle(job.getTitle());
        response.setDescription(job.getDescription());
        response.setRequirements(job.getRequirements());
        response.setLocation(job.getLocation());
        response.setSalaryMin(job.getSalaryMin());
        response.setSalaryMax(job.getSalaryMax());
        response.setJobType(job.getJobType());
        response.setRemote(job.getRemote());
        response.setStatus(job.getStatus());
        response.setCreatedAt(job.getCreatedAt());
        response.setUpdatedAt(job.getUpdatedAt());
        response.setExpiresAt(job.getExpiresAt());
        response.setRecruiterCompany(job.getRecruiter().getCompany());
        return response;
    }

    private ApplicationResponse convertToApplicationResponse(Application application) {
        ApplicationResponse response = new ApplicationResponse();
        response.setId(application.getId());
        response.setJob(convertToJobResponse(application.getJob()));
        response.setStatus(application.getStatus());
        response.setAppliedAt(application.getAppliedAt());
        response.setUpdatedAt(application.getUpdatedAt());
        response.setCoverLetter(application.getCoverLetter());
        response.setMatchScore(application.getMatchScore());
        return response;
    }
}