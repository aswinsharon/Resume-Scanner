package com.jobboard.service;

import com.jobboard.domain.Candidate;
import com.jobboard.domain.Recruiter;
import com.jobboard.domain.Role;
import com.jobboard.domain.User;
import com.jobboard.dto.auth.JwtAuthenticationResponse;
import com.jobboard.dto.auth.LoginRequest;
import com.jobboard.dto.auth.RegisterRequest;
import com.jobboard.dto.user.UserResponse;
import com.jobboard.exception.BadRequestException;
import com.jobboard.exception.ConflictException;
import com.jobboard.exception.ResourceNotFoundException;
import com.jobboard.exception.UnauthorizedException;
import com.jobboard.repository.CandidateRepository;
import com.jobboard.repository.RecruiterRepository;
import com.jobboard.repository.RoleRepository;
import com.jobboard.repository.UserRepository;
import com.jobboard.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private RecruiterRepository recruiterRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Transactional
    public JwtAuthenticationResponse register(RegisterRequest registerRequest) {
        try {
            logger.info("Attempting to register user with email: {}", registerRequest.getEmail());

            if (userRepository.existsByEmail(registerRequest.getEmail())) {
                throw new ConflictException("Email address already in use!");
            }

            // Validate role
            Role.RoleName roleName;
            try {
                roleName = Role.RoleName.valueOf(registerRequest.getRole());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid role: " + registerRequest.getRole());
            }

            // Create new user
            User user = new User();
            user.setEmail(registerRequest.getEmail());
            user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
            user.setFirstName(registerRequest.getFirstName());
            user.setLastName(registerRequest.getLastName());
            user.setPhone(registerRequest.getPhone());

            // Assign role
            Role userRole = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleName));

            user.addRole(userRole);

            User savedUser = userRepository.save(user);
            logger.info("User registered successfully with ID: {}", savedUser.getId());

            // Create role-specific profile
            createRoleSpecificProfile(savedUser, roleName, registerRequest);

            // Authenticate and generate tokens
            Authentication authentication = authenticateUser(registerRequest.getEmail(), registerRequest.getPassword());
            String jwt = tokenProvider.generateToken(authentication);
            String refreshToken = tokenProvider.generateRefreshToken(authentication);

            UserResponse userResponse = convertToUserResponse(savedUser);

            return new JwtAuthenticationResponse(jwt, refreshToken, userResponse);

        } catch (ConflictException | BadRequestException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during user registration", e);
            throw new RuntimeException("Registration failed due to an unexpected error", e);
        }
    }

    public JwtAuthenticationResponse login(LoginRequest loginRequest) {
        try {
            logger.info("Attempting to login user with email: {}", loginRequest.getEmail());

            Authentication authentication = authenticateUser(loginRequest.getEmail(), loginRequest.getPassword());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            String jwt = tokenProvider.generateToken(authentication);
            String refreshToken = tokenProvider.generateRefreshToken(authentication);

            User user = userRepository.findByEmailWithRoles(loginRequest.getEmail())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            UserResponse userResponse = convertToUserResponse(user);
            logger.info("User logged in successfully with ID: {}", user.getId());

            return new JwtAuthenticationResponse(jwt, refreshToken, userResponse);

        } catch (BadCredentialsException e) {
            logger.warn("Invalid credentials for email: {}", loginRequest.getEmail());
            throw e;
        } catch (AuthenticationException e) {
            logger.warn("Authentication failed for email: {}", loginRequest.getEmail());
            throw new UnauthorizedException("Authentication failed");
        } catch (Exception e) {
            logger.error("Unexpected error during login", e);
            throw new RuntimeException("Login failed due to an unexpected error", e);
        }
    }

    public JwtAuthenticationResponse refreshToken(String refreshToken) {
        try {
            if (refreshToken == null || refreshToken.trim().isEmpty()) {
                throw new BadRequestException("Refresh token is required");
            }

            if (!tokenProvider.validateToken(refreshToken) || !tokenProvider.isRefreshToken(refreshToken)) {
                throw new UnauthorizedException("Invalid refresh token");
            }

            Long userId = tokenProvider.getUserIdFromToken(refreshToken);
            User user = userRepository.findByIdWithRoles(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            if (!user.getIsActive()) {
                throw new UnauthorizedException("User account is inactive");
            }

            // Create new authentication
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    user.getEmail(), null, null);

            String newJwt = tokenProvider.generateToken(authentication);
            String newRefreshToken = tokenProvider.generateRefreshToken(authentication);

            UserResponse userResponse = convertToUserResponse(user);
            logger.info("Token refreshed successfully for user ID: {}", userId);

            return new JwtAuthenticationResponse(newJwt, newRefreshToken, userResponse);

        } catch (BadRequestException | UnauthorizedException | ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during token refresh", e);
            throw new RuntimeException("Token refresh failed due to an unexpected error", e);
        }
    }

    private Authentication authenticateUser(String email, String password) {
        try {
            return authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password));
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid email or password");
        } catch (AuthenticationException e) {
            throw new UnauthorizedException("Authentication failed");
        }
    }

    private void createRoleSpecificProfile(User user, Role.RoleName roleName, RegisterRequest registerRequest) {
        try {
            if (roleName == Role.RoleName.CANDIDATE) {
                Candidate candidate = new Candidate(user);
                candidateRepository.save(candidate);
                logger.info("Candidate profile created for user ID: {}", user.getId());
            } else if (roleName == Role.RoleName.RECRUITER) {
                if (registerRequest.getCompany() == null || registerRequest.getCompany().trim().isEmpty()) {
                    throw new BadRequestException("Company is required for recruiter registration");
                }
                Recruiter recruiter = new Recruiter(user, registerRequest.getCompany());
                recruiter.setDepartment(registerRequest.getDepartment());
                recruiterRepository.save(recruiter);
                logger.info("Recruiter profile created for user ID: {}", user.getId());
            }
        } catch (Exception e) {
            logger.error("Error creating role-specific profile for user ID: {}", user.getId(), e);
            throw new RuntimeException("Failed to create user profile", e);
        }
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
}