package com.jobboard.security;

import com.jobboard.domain.Role;
import com.jobboard.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private Authentication authentication;

    private UserPrincipal userPrincipal;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(
                "mySecretKey123456789012345678901234567890",
                86400000, // 24 hours
                604800000 // 7 days
        );

        // Create test user
        User user = new User("test@example.com", "password", "John", "Doe");
        user.setId(1L);

        Role candidateRole = new Role(Role.RoleName.CANDIDATE);
        user.setRoles(Set.of(candidateRole));

        userPrincipal = UserPrincipal.create(user);
    }

    @Test
    void shouldGenerateValidAccessToken() {
        // Given
        when(authentication.getPrincipal()).thenReturn(userPrincipal);

        // When
        String token = jwtTokenProvider.generateToken(authentication);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    void shouldGenerateValidRefreshToken() {
        // Given
        when(authentication.getPrincipal()).thenReturn(userPrincipal);

        // When
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        // Then
        assertThat(refreshToken).isNotNull();
        assertThat(refreshToken).isNotEmpty();
        assertTrue(jwtTokenProvider.validateToken(refreshToken));
        assertTrue(jwtTokenProvider.isRefreshToken(refreshToken));
    }

    @Test
    void shouldExtractUserIdFromToken() {
        // Given
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        String token = jwtTokenProvider.generateToken(authentication);

        // When
        Long userId = jwtTokenProvider.getUserIdFromToken(token);

        // Then
        assertThat(userId).isEqualTo(1L);
    }

    @Test
    void shouldValidateValidToken() {
        // Given
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        String token = jwtTokenProvider.generateToken(authentication);

        // When
        boolean isValid = jwtTokenProvider.validateToken(token);

        // Then
        assertTrue(isValid);
    }

    @Test
    void shouldRejectInvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    void shouldRejectNullToken() {
        // When
        boolean isValid = jwtTokenProvider.validateToken(null);

        // Then
        assertFalse(isValid);
    }

    @Test
    void shouldRejectEmptyToken() {
        // When
        boolean isValid = jwtTokenProvider.validateToken("");

        // Then
        assertFalse(isValid);
    }

    @Test
    void shouldIdentifyRefreshToken() {
        // Given
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);
        String accessToken = jwtTokenProvider.generateToken(authentication);

        // When & Then
        assertTrue(jwtTokenProvider.isRefreshToken(refreshToken));
        assertFalse(jwtTokenProvider.isRefreshToken(accessToken));
    }

    @Test
    void shouldRejectExpiredToken() {
        // Given - Create provider with very short expiration
        JwtTokenProvider shortExpirationProvider = new JwtTokenProvider(
                "mySecretKey123456789012345678901234567890",
                1, // 1 millisecond
                1000);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        String token = shortExpirationProvider.generateToken(authentication);

        // Wait for token to expire
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // When
        boolean isValid = shortExpirationProvider.validateToken(token);

        // Then
        assertFalse(isValid);
    }
}