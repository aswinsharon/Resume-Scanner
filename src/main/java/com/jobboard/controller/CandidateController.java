package com.jobboard.controller;

import com.jobboard.controller.AuthController.ApiResponse;
import com.jobboard.domain.Education;
import com.jobboard.domain.Experience;
import com.jobboard.domain.Resume;
import com.jobboard.dto.application.ApplicationResponse;
import com.jobboard.dto.candidate.*;
import com.jobboard.dto.job.JobResponse;
import com.jobboard.security.UserPrincipal;
import com.jobboard.service.CandidateService;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/candidates")
@Tag(name = "Candidate", description = "Candidate management APIs")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('CANDIDATE')")
public class CandidateController {

    @Autowired
    private CandidateService candidateService;

    @GetMapping("/profile")
    @Operation(summary = "Get candidate profile")
    public ResponseEntity<ApiResponse<CandidateProfileResponse>> getProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        CandidateProfileResponse profile = candidateService.getProfile(userPrincipal.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, profile, "Profile retrieved successfully"));
    }

    @PutMapping("/profile")
    @Operation(summary = "Update candidate profile")
    public ResponseEntity<ApiResponse<CandidateProfileResponse>> updateProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody CandidateProfileRequest request) {

        CandidateProfileResponse profile = candidateService.updateProfile(userPrincipal.getId(), request);
        return ResponseEntity.ok(new ApiResponse<>(true, profile, "Profile updated successfully"));
    }

    @PostMapping("/resume")
    @Operation(summary = "Upload resume")
    public ResponseEntity<ApiResponse<Resume>> uploadResume(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam("file") MultipartFile file) throws IOException {

        Resume resume = candidateService.uploadResume(userPrincipal.getId(), file);
        return ResponseEntity.ok(new ApiResponse<>(true, resume, "Resume uploaded successfully"));
    }

    @GetMapping("/resume")
    @Operation(summary = "Get current resume")
    public ResponseEntity<ApiResponse<Resume>> getCurrentResume(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        Resume resume = candidateService.getCurrentResume(userPrincipal.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, resume, "Resume retrieved successfully"));
    }

    @PostMapping("/education")
    @Operation(summary = "Add education")
    public ResponseEntity<ApiResponse<Education>> addEducation(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody EducationRequest request) {

        Education education = candidateService.addEducation(userPrincipal.getId(), request);
        return ResponseEntity.ok(new ApiResponse<>(true, education, "Education added successfully"));
    }

    @PostMapping("/experience")
    @Operation(summary = "Add work experience")
    public ResponseEntity<ApiResponse<Experience>> addExperience(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody ExperienceRequest request) {

        Experience experience = candidateService.addExperience(userPrincipal.getId(), request);
        return ResponseEntity.ok(new ApiResponse<>(true, experience, "Experience added successfully"));
    }

    @GetMapping("/jobs/recommended")
    @Operation(summary = "Get recommended jobs")
    public ResponseEntity<ApiResponse<List<JobResponse>>> getRecommendedJobs(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            Pageable pageable) {

        List<JobResponse> jobs = candidateService.getRecommendedJobs(userPrincipal.getId(), pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, jobs, "Recommended jobs retrieved successfully"));
    }

    @PostMapping("/jobs/{jobId}/apply")
    @Operation(summary = "Apply for a job")
    public ResponseEntity<ApiResponse<String>> applyForJob(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long jobId,
            @Valid @RequestBody ApplicationRequest request) {

        candidateService.applyForJob(userPrincipal.getId(), jobId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, null, "Application submitted successfully"));
    }

    @GetMapping("/applications")
    @Operation(summary = "Get candidate's applications")
    public ResponseEntity<ApiResponse<Page<ApplicationResponse>>> getApplications(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            Pageable pageable) {

        Page<ApplicationResponse> applications = candidateService.getApplications(userPrincipal.getId(), pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, applications, "Applications retrieved successfully"));
    }
}