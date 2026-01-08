package com.jobboard.controller;

import com.jobboard.controller.AuthController.ApiResponse;
import com.jobboard.dto.recruiter.RecruiterProfileRequest;
import com.jobboard.dto.recruiter.RecruiterProfileResponse;
import com.jobboard.security.UserPrincipal;
import com.jobboard.service.RecruiterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recruiters")
@Tag(name = "Recruiter", description = "Recruiter management APIs")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('RECRUITER')")
public class RecruiterController {

    @Autowired
    private RecruiterService recruiterService;

    @GetMapping("/profile")
    @Operation(summary = "Get recruiter profile")
    public ResponseEntity<ApiResponse<RecruiterProfileResponse>> getProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        RecruiterProfileResponse profile = recruiterService.getProfile(userPrincipal.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, profile, "Profile retrieved successfully"));
    }

    @PutMapping("/profile")
    @Operation(summary = "Update recruiter profile")
    public ResponseEntity<ApiResponse<RecruiterProfileResponse>> updateProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody RecruiterProfileRequest request) {

        RecruiterProfileResponse profile = recruiterService.updateProfile(userPrincipal.getId(), request);
        return ResponseEntity.ok(new ApiResponse<>(true, profile, "Profile updated successfully"));
    }
}