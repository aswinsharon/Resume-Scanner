package com.jobboard.service;

import com.jobboard.domain.*;
import com.jobboard.dto.application.ApplicationResponse;
import com.jobboard.dto.job.*;
import com.jobboard.dto.user.UserResponse;
import com.jobboard.exception.BadRequestException;
import com.jobboard.exception.ResourceNotFoundException;
import com.jobboard.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class JobService {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private RecruiterRepository recruiterRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private MatchScoreRepository matchScoreRepository;

    @Autowired
    private MatchingService matchingService;

    public JobResponse createJob(Long userId, JobRequest request) {
        Recruiter recruiter = recruiterRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Recruiter profile not found"));

        Job job = new Job(recruiter, request.getTitle(), request.getDescription());
        job.setRequirements(request.getRequirements());
        job.setLocation(request.getLocation());
        job.setSalaryMin(request.getSalaryMin());
        job.setSalaryMax(request.getSalaryMax());
        job.setJobType(request.getJobType());
        job.setRemote(request.getRemote());
        job.setExpiresAt(request.getExpiresAt());

        Job savedJob = jobRepository.save(job);

        // Add job skills
        if (request.getSkills() != null && !request.getSkills().isEmpty()) {
            List<JobSkill> jobSkills = request.getSkills().stream()
                    .map(skillRequest -> {
                        JobSkill jobSkill = new JobSkill(savedJob, skillRequest.getSkillName(),
                                skillRequest.getRequired());
                        jobSkill.setMinYears(skillRequest.getMinYears());
                        jobSkill.setWeight(skillRequest.getWeight());
                        return jobSkill;
                    })
                    .collect(Collectors.toList());

            savedJob.setJobSkills(jobSkills);
        }

        return convertToJobResponse(savedJob);
    }

    public Page<JobResponse> getRecruiterJobs(Long userId, Pageable pageable) {
        Recruiter recruiter = recruiterRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Recruiter profile not found"));

        Page<Job> jobs = jobRepository.findByRecruiterId(recruiter.getId(), pageable);
        return jobs.map(this::convertToJobResponse);
    }

    public JobResponse getJob(Long jobId) {
        Job job = jobRepository.findByIdWithSkills(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        return convertToJobResponse(job);
    }

    public JobResponse updateJob(Long userId, Long jobId, JobRequest request) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        // Verify ownership
        if (!job.getRecruiter().getUser().getId().equals(userId)) {
            throw new BadRequestException("You can only update your own job postings");
        }

        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setRequirements(request.getRequirements());
        job.setLocation(request.getLocation());
        job.setSalaryMin(request.getSalaryMin());
        job.setSalaryMax(request.getSalaryMax());
        job.setJobType(request.getJobType());
        job.setRemote(request.getRemote());
        job.setExpiresAt(request.getExpiresAt());

        // Update job skills
        if (request.getSkills() != null) {
            job.getJobSkills().clear();
            List<JobSkill> jobSkills = request.getSkills().stream()
                    .map(skillRequest -> {
                        JobSkill jobSkill = new JobSkill(job, skillRequest.getSkillName(), skillRequest.getRequired());
                        jobSkill.setMinYears(skillRequest.getMinYears());
                        jobSkill.setWeight(skillRequest.getWeight());
                        return jobSkill;
                    })
                    .collect(Collectors.toList());

            job.setJobSkills(jobSkills);
        }

        Job savedJob = jobRepository.save(job);
        return convertToJobResponse(savedJob);
    }

    public void deleteJob(Long userId, Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        // Verify ownership
        if (!job.getRecruiter().getUser().getId().equals(userId)) {
            throw new BadRequestException("You can only delete your own job postings");
        }

        jobRepository.delete(job);
    }

    public List<CandidateMatchResponse> getRankedCandidates(Long userId, Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        // Verify ownership
        if (!job.getRecruiter().getUser().getId().equals(userId)) {
            throw new BadRequestException("You can only view candidates for your own job postings");
        }

        List<MatchScore> matchScores = matchScoreRepository.findByJobIdWithCandidateDetails(jobId);

        return matchScores.stream()
                .map(this::convertToCandidateMatchResponse)
                .collect(Collectors.toList());
    }

    public Page<ApplicationResponse> getJobApplications(Long userId, Long jobId, Pageable pageable) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        // Verify ownership
        if (!job.getRecruiter().getUser().getId().equals(userId)) {
            throw new BadRequestException("You can only view applications for your own job postings");
        }

        Page<Application> applications = applicationRepository.findByJobIdOrderByMatchScoreDesc(jobId, pageable);
        return applications.map(this::convertToApplicationResponse);
    }

    public Application updateApplicationStatus(Long userId, Long applicationId, Application.ApplicationStatus status) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        // Verify ownership
        if (!application.getJob().getRecruiter().getUser().getId().equals(userId)) {
            throw new BadRequestException("You can only update applications for your own job postings");
        }

        application.setStatus(status);
        return applicationRepository.save(application);
    }

    public Page<JobResponse> searchJobs(String title, String location, Job.JobType jobType,
            Boolean remote, BigDecimal salaryMin, Pageable pageable) {
        Page<Job> jobs = jobRepository.searchJobs(title, location, jobType, remote, salaryMin, pageable);
        return jobs.map(this::convertToJobResponse);
    }

    public JobResponse getPublicJob(Long jobId) {
        Job job = jobRepository.findByIdWithSkills(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));

        if (job.getStatus() != Job.JobStatus.ACTIVE) {
            throw new ResourceNotFoundException("Job is not available");
        }

        return convertToJobResponse(job);
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

        if (job.getJobSkills() != null) {
            List<JobSkillResponse> skills = job.getJobSkills().stream()
                    .map(skill -> new JobSkillResponse(
                            skill.getId(),
                            skill.getSkillName(),
                            skill.getRequired(),
                            skill.getMinYears(),
                            skill.getWeight()))
                    .collect(Collectors.toList());
            response.setSkills(skills);
        }

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

    private CandidateMatchResponse convertToCandidateMatchResponse(MatchScore matchScore) {
        Candidate candidate = matchScore.getCandidate();
        User user = candidate.getUser();

        CandidateMatchResponse response = new CandidateMatchResponse();
        response.setCandidateId(candidate.getId());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setEmail(user.getEmail());
        response.setLocation(candidate.getLocation());
        response.setTotalScore(matchScore.getTotalScore());
        response.setSkillScore(matchScore.getSkillScore());
        response.setExperienceScore(matchScore.getExpScore());
        response.setEducationScore(matchScore.getEduScore());
        response.setCalculatedAt(matchScore.getCalculatedAt());

        // Check if candidate has applied
        boolean hasApplied = applicationRepository.existsByCandidateIdAndJobId(
                candidate.getId(), matchScore.getJob().getId());
        response.setHasApplied(hasApplied);

        if (hasApplied) {
            Application application = applicationRepository.findByCandidateIdAndJobId(
                    candidate.getId(), matchScore.getJob().getId()).orElse(null);
            if (application != null) {
                response.setApplicationStatus(application.getStatus());
                response.setAppliedAt(application.getAppliedAt());
            }
        }

        return response;
    }
}