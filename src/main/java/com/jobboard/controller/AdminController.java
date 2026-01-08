package com.jobboard.controller;

import com.jobboard.controller.AuthController.ApiResponse;
import com.jobboard.dto.admin.PlatformStatsResponse;
import com.jobboard.dto.user.UserResponse;
import com.jobboard.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin", description = "Admin management APIs")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/users")
    @Operation(summary = "Get all users (paginated)")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(Pageable pageable) {
        Page<UserResponse> users = adminService.getAllUsers(pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, users, "Users retrieved successfully"));
    }

    @PutMapping("/users/{userId}/status")
    @Operation(summary = "Activate/deactivate user")
    public ResponseEntity<ApiResponse<String>> updateUserStatus(
            @PathVariable Long userId,
            @RequestBody Map<String, Boolean> request) {

        Boolean isActive = request.get("isActive");
        adminService.updateUserStatus(userId, isActive);
        return ResponseEntity.ok(new ApiResponse<>(true, null, "User status updated successfully"));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get platform statistics")
    public ResponseEntity<ApiResponse<PlatformStatsResponse>> getPlatformStats() {
        PlatformStatsResponse stats = adminService.getPlatformStats();
        return ResponseEntity.ok(new ApiResponse<>(true, stats, "Platform statistics retrieved successfully"));
    }
}