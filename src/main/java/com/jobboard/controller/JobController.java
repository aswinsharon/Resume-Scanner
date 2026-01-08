package com.jobboard.controller;

import com.jobboard.controller.AuthController.ApiResponse;
import com.jobboard.domain.Application;
import com.jobboard.domain.Job;
import com.jobboard.dto.application.ApplicationResponse;
import com.jobboard.dto.job.CandidateMatchResponse;
import com.jobboard.dto.job.JobRequest;
import com.jobboard.dto.job.JobResponse;
import com.jobboard.security.UserPrincipal;
import com.jobboard.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jobs")
@Tag(name = "Job", description = "Job management APIs")
public class JobController {

    @Autowired
    private JobService jobService;

    // Public endpoints
    @GetMapping("/search")
    @Operation(summary = "Search jobs (public)")
    public ResponseEntity<ApiResponse<Page<JobResponse>>> searchJobs(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Job.JobType jobType,
            @RequestParam(required = false) Boolean remote,
            @RequestParam(required = false) BigDecimal salaryMin,
            Pageable pageable) {

        Page<JobResponse> jobs = jobService.searchJobs(title, location, jobType, remote, salaryMin, pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, jobs, "Jobs retrieved successfully"));
    }

    @GetMapping("/{jobId}/public")
    @Operation(summary = "Get public job details")
    public ResponseEntity<ApiResponse<JobResponse>> getPublicJob(@PathVariable Long jobId) {
        JobResponse job = jobService.getPublicJob(jobId);
        return ResponseEntity.ok(new ApiResponse<>(true, job, "Job retrieved successfully"));
    }

    // Recruiter endpoints
    @PostMapping
    @Operation(summary = "Create job posting")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<ApiResponse<JobResponse>> createJob(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody JobRequest request) {

        JobResponse job = jobService.createJob(userPrincipal.getId(), request);
        return ResponseEntity.ok(new ApiResponse<>(true, job, "Job created successfully"));
    }

    @GetMapping
    @Operation(summary = "Get recruiter's job postings")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<ApiResponse<Page<JobResponse>>> getRecruiterJobs(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            Pageable pageable) {

        Page<JobResponse> jobs = jobService.getRecruiterJobs(userPrincipal.getId(), pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, jobs, "Jobs retrieved successfully"));
    }

    @GetMapping("/{jobId}")
    @Operation(summary = "Get job details")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('RECRUITER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<JobResponse>> getJob(@PathVariable Long jobId) {
        JobResponse job = jobService.getJob(jobId);
        return ResponseEntity.ok(new ApiResponse<>(true, job, "Job retrieved successfully"));
    }

    @PutMapping("/{jobId}")
    @Operation(summary = "Update job posting")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<ApiResponse<JobResponse>> updateJob(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long jobId,
            @Valid @RequestBody JobRequest request) {

        JobResponse job = jobService.updateJob(userPrincipal.getId(), jobId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, job, "Job updated successfully"));
    }

    @DeleteMapping("/{jobId}")
    @Operation(summary = "Delete job posting")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<ApiResponse<String>> deleteJob(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long jobId) {

        jobService.deleteJob(userPrincipal.getId(), jobId);
        return ResponseEntity.ok(new ApiResponse<>(true, null, "Job deleted successfully"));
    }

    @GetMapping("/{jobId}/candidates")
    @Operation(summary = "Get ranked candidates for job")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<ApiResponse<List<CandidateMatchResponse>>> getRankedCandidates(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long jobId) {

        List<CandidateMatchResponse> candidates = jobService.getRankedCandidates(userPrincipal.getId(), jobId);
        return ResponseEntity.ok(new ApiResponse<>(true, candidates, "Candidates retrieved successfully"));
    }

    @GetMapping("/{jobId}/applications")
    @Operation(summary = "Get applications for job")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<ApiResponse<Page<ApplicationResponse>>> getJobApplications(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long jobId,
            Pageable pageable) {

        Page<ApplicationResponse> applications = jobService.getJobApplications(userPrincipal.getId(), jobId, pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, applications, "Applications retrieved successfully"));
    }

    @PutMapping("/applications/{applicationId}/status")
    @Operation(summary = "Update application status")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<ApiResponse<String>> updateApplicationStatus(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long applicationId,
            @RequestBody Map<String, String> request) {

        Application.ApplicationStatus status = Application.ApplicationStatus.valueOf(request.get("status"));
        jobService.updateApplicationStatus(userPrincipal.getId(), applicationId, status);
        return ResponseEntity.ok(new ApiResponse<>(true, null, "Application status updated successfully"));
    }
}