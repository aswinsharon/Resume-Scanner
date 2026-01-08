package com.jobboard.repository;

import com.jobboard.domain.Role;
import com.jobboard.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private User testUser;
    private Role candidateRole;

    @BeforeEach
    void setUp() {
        // Create and persist role
        candidateRole = new Role(Role.RoleName.CANDIDATE, "Job seeker role");
        entityManager.persistAndFlush(candidateRole);

        // Create test user
        testUser = new User("test@example.com", "password123", "John", "Doe");
        testUser.setPhone("+1234567890");
        testUser.addRole(candidateRole);
    }

    @Test
    void shouldSaveAndFindUserByEmail() {
        // Given
        entityManager.persistAndFlush(testUser);

        // When
        Optional<User> found = userRepository.findByEmail("test@example.com");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
        assertThat(found.get().getFirstName()).isEqualTo("John");
        assertThat(found.get().getLastName()).isEqualTo("Doe");
    }

    @Test
    void shouldReturnEmptyWhenUserNotFound() {
        // When
        Optional<User> found = userRepository.findByEmail("nonexistent@example.com");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void shouldCheckIfEmailExists() {
        // Given
        entityManager.persistAndFlush(testUser);

        // When & Then
        assertThat(userRepository.existsByEmail("test@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("nonexistent@example.com")).isFalse();
    }

    @Test
    void shouldFindUserByEmailWithRoles() {
        // Given
        entityManager.persistAndFlush(testUser);

        // When
        Optional<User> found = userRepository.findByEmailWithRoles("test@example.com");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getRoles()).hasSize(1);
        assertThat(found.get().getRoles().iterator().next().getName()).isEqualTo(Role.RoleName.CANDIDATE);
    }

    @Test
    void shouldFindUserByIdWithRoles() {
        // Given
        User savedUser = entityManager.persistAndFlush(testUser);

        // When
        Optional<User> found = userRepository.findByIdWithRoles(savedUser.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getRoles()).hasSize(1);
        assertThat(found.get().getRoles().iterator().next().getName()).isEqualTo(Role.RoleName.CANDIDATE);
    }

    @Test
    void shouldHandleUserWithMultipleRoles() {
        // Given
        Role adminRole = new Role(Role.RoleName.ADMIN, "Administrator role");
        entityManager.persistAndFlush(adminRole);

        testUser.addRole(adminRole);
        entityManager.persistAndFlush(testUser);

        // When
        Optional<User> found = userRepository.findByEmailWithRoles("test@example.com");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getRoles()).hasSize(2);

        Set<Role.RoleName> roleNames = found.get().getRoles().stream()
                .map(Role::getName)
                .collect(java.util.stream.Collectors.toSet());

        assertThat(roleNames).contains(Role.RoleName.CANDIDATE, Role.RoleName.ADMIN);
    }

    @Test
    void shouldSaveUserWithIsActiveFlag() {
        // Given
        testUser.setIsActive(false);
        entityManager.persistAndFlush(testUser);

        // When
        Optional<User> found = userRepository.findByEmail("test@example.com");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getIsActive()).isFalse();
    }

    @Test
    void shouldSetDefaultIsActiveToTrue() {
        // Given
        User newUser = new User("new@example.com", "password", "Jane", "Smith");
        // isActive should default to true

        // When
        User saved = entityManager.persistAndFlush(newUser);

        // Then
        assertThat(saved.getIsActive()).isTrue();
    }
}