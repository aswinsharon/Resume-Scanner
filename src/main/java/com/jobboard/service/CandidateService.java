package com.jobboard.service;

import com.jobboard.domain.*;
import com.jobboard.dto.application.ApplicationResponse;
import com.jobboard.dto.candidate.*;
import com.jobboard.dto.job.JobResponse;
import com.jobboard.dto.user.UserResponse;
import com.jobboard.exception.BadRequestException;
import com.jobboard.exception.ResourceNotFoundException;
import com.jobboard.repository.*;

import org.apache.tika.exception.TikaException;
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

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private UserRepository userRepository;

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
        Candidate candidate = candidateRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found"));

        return convertToCandidateProfileResponse(candidate);
    }

    public CandidateProfileResponse updateProfile(Long userId, CandidateProfileRequest request) {
        Candidate candidate = candidateRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found"));

        candidate.setSummary(request.getSummary());
        candidate.setLocation(request.getLocation());
        candidate.setLinkedin(request.getLinkedin());
        candidate.setGithub(request.getGithub());
        candidate.setWebsite(request.getWebsite());

        Candidate savedCandidate = candidateRepository.save(candidate);
        return convertToCandidateProfileResponse(savedCandidate);
    }

    public Resume uploadResume(Long userId, MultipartFile file) throws IOException, TikaException {
        Candidate candidate = candidateRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found"));

        // Validate file
        if (file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }

        String contentType = file.getContentType();
        if (!"application/pdf".equals(contentType)) {
            throw new BadRequestException("Only PDF files are allowed");
        }

        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
        Path filePath = uploadPath.resolve(uniqueFilename);

        // Save file
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Parse resume content
        String parsedText = resumeParsingService.extractTextFromFile(file);
        Map<String, Object> structuredData = resumeParsingService.extractStructuredData(parsedText);

        // Create resume record
        Resume resume = new Resume(candidate, filePath.toString(), originalFilename);
        resume.setParsedText(parsedText);
        resume.setSkillsJson(structuredData);

        Resume savedResume = resumeRepository.save(resume);

        // Extract and save skills
        List<ResumeSkill> skills = resumeParsingService.extractSkills(savedResume, parsedText);
        savedResume.setResumeSkills(skills);

        return savedResume;
    }

    public Resume getCurrentResume(Long userId) {
        Candidate candidate = candidateRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found"));

        return candidate.getResumes().stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No resume found"));
    }

    public Education addEducation(Long userId, EducationRequest request) {
        Candidate candidate = candidateRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found"));

        Education education = new Education(candidate, request.getDegree(), request.getField(),
                request.getInstitution());
        education.setStartDate(request.getStartDate());
        education.setEndDate(request.getEndDate());
        education.setGpa(request.getGpa());

        return educationRepository.save(education);
    }

    public Experience addExperience(Long userId, ExperienceRequest request) {
        Candidate candidate = candidateRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found"));

        Experience experience = new Experience(candidate, request.getCompany(), request.getPosition());
        experience.setDescription(request.getDescription());
        experience.setStartDate(request.getStartDate());
        experience.setEndDate(request.getEndDate());
        experience.setIsCurrent(request.getIsCurrent());

        return experienceRepository.save(experience);
    }

    public List<JobResponse> getRecommendedJobs(Long userId, Pageable pageable) {
        Candidate candidate = candidateRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found"));

        // Get candidate skills
        List<String> candidateSkills = candidate.getResumes().stream()
                .flatMap(resume -> resume.getResumeSkills().stream())
                .map(ResumeSkill::getSkillName)
                .collect(Collectors.toList());

        if (candidateSkills.isEmpty()) {
            return List.of();
        }

        // Find jobs with matching skills
        List<Job> matchingJobs = jobRepository.findJobsBySkills(candidateSkills);

        return matchingJobs.stream()
                .map(this::convertToJobResponse)
                .collect(Collectors.toList());
    }

    public Application applyForJob(Long userId, Long jobId, ApplicationRequest request) {
        Candidate candidate = candidateRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found"));

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        // Check if already applied
        if (applicationRepository.existsByCandidateIdAndJobId(candidate.getId(), jobId)) {
            throw new BadRequestException("You have already applied for this job");
        }

        // Create application
        Application application = new Application(candidate, job);
        application.setCoverLetter(request.getCoverLetter());

        // Calculate match score
        MatchScore matchScore = matchingService.calculateMatchScore(candidate, job);
        application.setMatchScore(matchScore.getTotalScore());

        return applicationRepository.save(application);
    }

    public Page<ApplicationResponse> getApplications(Long userId, Pageable pageable) {
        Candidate candidate = candidateRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found"));

        Page<Application> applications = applicationRepository.findByCandidateIdOrderByAppliedAtDesc(candidate.getId(),
                pageable);

        return applications.map(this::convertToApplicationResponse);
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