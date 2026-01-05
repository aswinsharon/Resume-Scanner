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
import com.jobboard.repository.CandidateRepository;
import com.jobboard.repository.RecruiterRepository;
import com.jobboard.repository.RoleRepository;
import com.jobboard.repository.UserRepository;
import com.jobboard.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
public class AuthService {

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
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new BadRequestException("Email address already in use!");
        }

        // Create new user
        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setPhone(registerRequest.getPhone());

        // Assign role
        Role.RoleName roleName = Role.RoleName.valueOf(registerRequest.getRole());
        Role userRole = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("User Role not set."));

        user.addRole(userRole);

        User savedUser = userRepository.save(user);

        // Create role-specific profile
        if (roleName == Role.RoleName.CANDIDATE) {
            Candidate candidate = new Candidate(savedUser);
            candidateRepository.save(candidate);
        } else if (roleName == Role.RoleName.RECRUITER) {
            Recruiter recruiter = new Recruiter(savedUser, registerRequest.getCompany());
            recruiter.setDepartment(registerRequest.getDepartment());
            recruiterRepository.save(recruiter);
        }

        // Authenticate and generate tokens
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        registerRequest.getEmail(),
                        registerRequest.getPassword()));

        String jwt = tokenProvider.generateToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(authentication);

        UserResponse userResponse = convertToUserResponse(savedUser);

        return new JwtAuthenticationResponse(jwt, refreshToken, userResponse);
    }

    public JwtAuthenticationResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.generateToken(authentication);
        String refreshToken = tokenProvider.generateRefreshToken(authentication);

        User user = userRepository.findByEmailWithRoles(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserResponse userResponse = convertToUserResponse(user);

        return new JwtAuthenticationResponse(jwt, refreshToken, userResponse);
    }

    public JwtAuthenticationResponse refreshToken(String refreshToken) {
        if (!tokenProvider.validateToken(refreshToken) || !tokenProvider.isRefreshToken(refreshToken)) {
            throw new BadRequestException("Invalid refresh token");
        }

        Long userId = tokenProvider.getUserIdFromToken(refreshToken);
        User user = userRepository.findByIdWithRoles(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Create new authentication
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getEmail(), null, null);

        String newJwt = tokenProvider.generateToken(authentication);
        String newRefreshToken = tokenProvider.generateRefreshToken(authentication);

        UserResponse userResponse = convertToUserResponse(user);

        return new JwtAuthenticationResponse(newJwt, newRefreshToken, userResponse);
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