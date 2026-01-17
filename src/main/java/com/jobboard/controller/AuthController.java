package com.jobboard.controller;

import com.jobboard.dto.auth.JwtAuthenticationResponse;
import com.jobboard.dto.auth.LoginRequest;
import com.jobboard.dto.auth.RegisterRequest;
import com.jobboard.dto.common.ApiResponse;
import com.jobboard.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<ApiResponse<JwtAuthenticationResponse>> register(
            @Valid @RequestBody RegisterRequest registerRequest) {

        JwtAuthenticationResponse response = authService.register(registerRequest);
        return ResponseEntity.ok(ApiResponse.success(response, "User registered successfully"));
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and return JWT token")
    public ResponseEntity<ApiResponse<JwtAuthenticationResponse>> login(
            @Valid @RequestBody LoginRequest loginRequest) {

        JwtAuthenticationResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(ApiResponse.success(response, "User authenticated successfully"));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh JWT token")
    public ResponseEntity<ApiResponse<JwtAuthenticationResponse>> refreshToken(
            @RequestBody Map<String, String> request) {

        String refreshToken = request.get("refreshToken");
        JwtAuthenticationResponse response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(ApiResponse.success(response, "Token refreshed successfully"));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user")
    public ResponseEntity<ApiResponse<String>> logout() {
        // In a real implementation, you might want to blacklist the token
        logger.info("User logout requested");
        return ResponseEntity.ok(ApiResponse.success("User logged out successfully"));
    }
}