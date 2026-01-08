package com.jobboard.service;

import com.jobboard.domain.Candidate;
import com.jobboard.domain.Recruiter;
import com.jobboard.domain.Role;
import com.jobboard.domain.User;
import com.jobboard.dto.auth.JwtAuthenticationResponse;
import com.jobboard.dto.auth.LoginRequest;
import com.jobboard.dto.auth.RegisterRequest;
import com.jobboard.exception.BadRequestException;
import com.jobboard.repository.CandidateRepository;
import com.jobboard.repository.RecruiterRepository;
import com.jobboard.repository.RoleRepository;
import com.jobboard.repository.UserRepository;
import com.jobboard.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private CandidateRepository candidateRepository;

    @Mock
    private RecruiterRepository recruiterRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest candidateRegisterRequest;
    private RegisterRequest recruiterRegisterRequest;
    private LoginRequest loginRequest;
    private User testUser;
    private Role candidateRole;
    private Role recruiterRole;

    @BeforeEach
    void setUp() {
        candidateRegisterRequest = new RegisterRequest();
        candidateRegisterRequest.setEmail("candidate@example.com");
        candidateRegisterRequest.setPassword("password123");
        candidateRegisterRequest.setFirstName("John");
        candidateRegisterRequest.setLastName("Doe");
        candidateRegisterRequest.setRole("CANDIDATE");

        recruiterRegisterRequest = new RegisterRequest();
        recruiterRegisterRequest.setEmail("recruiter@example.com");
        recruiterRegisterRequest.setPassword("password123");
        recruiterRegisterRequest.setFirstName("Jane");
        recruiterRegisterRequest.setLastName("Smith");
        recruiterRegisterRequest.setRole("RECRUITER");
        recruiterRegisterRequest.setCompany("Tech Corp");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        testUser = new User("test@example.com", "encodedPassword", "John", "Doe");
        testUser.setId(1L);

        candidateRole = new Role(Role.RoleName.CANDIDATE);
        candidateRole.setId(1L);

        recruiterRole = new Role(Role.RoleName.RECRUITER);
        recruiterRole.setId(2L);
    }

    @Test
    void shouldRegisterCandidateSuccessfully() {
        // Given
        when(userRepository.existsByEmail(candidateRegisterRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(candidateRegisterRequest.getPassword())).thenReturn("encodedPassword");
        when(roleRepository.findByName(Role.RoleName.CANDIDATE)).thenReturn(Optional.of(candidateRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(candidateRepository.save(any(Candidate.class))).thenReturn(new Candidate());
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(tokenProvider.generateToken(authentication)).thenReturn("access-token");
        when(tokenProvider.generateRefreshToken(authentication)).thenReturn("refresh-token");

        // When
        JwtAuthenticationResponse response = authService.register(candidateRegisterRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getUser()).isNotNull();

        verify(userRepository).save(any(User.class));
        verify(candidateRepository).save(any(Candidate.class));
        verify(recruiterRepository, never()).save(any(Recruiter.class));
    }

    @Test
    void shouldRegisterRecruiterSuccessfully() {
        // Given
        when(userRepository.existsByEmail(recruiterRegisterRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(recruiterRegisterRequest.getPassword())).thenReturn("encodedPassword");
        when(roleRepository.findByName(Role.RoleName.RECRUITER)).thenReturn(Optional.of(recruiterRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(recruiterRepository.save(any(Recruiter.class))).thenReturn(new Recruiter());
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(tokenProvider.generateToken(authentication)).thenReturn("access-token");
        when(tokenProvider.generateRefreshToken(authentication)).thenReturn("refresh-token");

        // When
        JwtAuthenticationResponse response = authService.register(recruiterRegisterRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");

        verify(userRepository).save(any(User.class));
        verify(recruiterRepository).save(any(Recruiter.class));
        verify(candidateRepository, never()).save(any(Candidate.class));
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        // Given
        when(userRepository.existsByEmail(candidateRegisterRequest.getEmail())).thenReturn(true);

        // When & Then
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> authService.register(candidateRegisterRequest));

        assertThat(exception.getMessage()).isEqualTo("Email address already in use!");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldLoginSuccessfully() {
        // Given
        testUser.setRoles(Set.of(candidateRole));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(tokenProvider.generateToken(authentication)).thenReturn("access-token");
        when(tokenProvider.generateRefreshToken(authentication)).thenReturn("refresh-token");
        when(userRepository.findByEmailWithRoles(loginRequest.getEmail())).thenReturn(Optional.of(testUser));

        // When
        JwtAuthenticationResponse response = authService.login(loginRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getUser().getEmail()).isEqualTo(testUser.getEmail());
    }

    @Test
    void shouldRefreshTokenSuccessfully() {
        // Given
        String refreshToken = "valid-refresh-token";
        testUser.setRoles(Set.of(candidateRole));

        when(tokenProvider.validateToken(refreshToken)).thenReturn(true);
        when(tokenProvider.isRefreshToken(refreshToken)).thenReturn(true);
        when(tokenProvider.getUserIdFromToken(refreshToken)).thenReturn(1L);
        when(userRepository.findByIdWithRoles(1L)).thenReturn(Optional.of(testUser));
        when(tokenProvider.generateToken(any(Authentication.class))).thenReturn("new-access-token");
        when(tokenProvider.generateRefreshToken(any(Authentication.class))).thenReturn("new-refresh-token");

        // When
        JwtAuthenticationResponse response = authService.refreshToken(refreshToken);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("new-refresh-token");
    }

    @Test
    void shouldThrowExceptionForInvalidRefreshToken() {
        // Given
        String invalidRefreshToken = "invalid-refresh-token";
        when(tokenProvider.validateToken(invalidRefreshToken)).thenReturn(false);

        // When & Then
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> authService.refreshToken(invalidRefreshToken));

        assertThat(exception.getMessage()).isEqualTo("Invalid refresh token");
    }

    @Test
    void shouldThrowExceptionForAccessTokenUsedAsRefreshToken() {
        // Given
        String accessToken = "access-token";
        when(tokenProvider.validateToken(accessToken)).thenReturn(true);
        when(tokenProvider.isRefreshToken(accessToken)).thenReturn(false);

        // When & Then
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> authService.refreshToken(accessToken));

        assertThat(exception.getMessage()).isEqualTo("Invalid refresh token");
    }
}