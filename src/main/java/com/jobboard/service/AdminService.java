package com.jobboard.service;

import com.jobboard.domain.Job;
import com.jobboard.domain.User;
import com.jobboard.dto.admin.PlatformStatsResponse;
import com.jobboard.dto.user.UserResponse;
import com.jobboard.exception.ResourceNotFoundException;
import com.jobboard.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@Transactional
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private RecruiterRepository recruiterRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private ResumeRepository resumeRepository;

    public Page<UserResponse> getAllUsers(Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable);
        return users.map(this::convertToUserResponse);
    }

    public void updateUserStatus(Long userId, Boolean isActive) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setIsActive(isActive);
        userRepository.save(user);
    }

    public PlatformStatsResponse getPlatformStats() {
        Long totalUsers = userRepository.count();
        Long totalCandidates = candidateRepository.count();
        Long totalRecruiters = recruiterRepository.count();
        Long totalJobs = jobRepository.count();
        Long activeJobs = jobRepository.countByStatus(Job.JobStatus.ACTIVE);
        Long totalApplications = applicationRepository.count();
        Long totalResumes = resumeRepository.count();

        return new PlatformStatsResponse(
                totalUsers,
                totalCandidates,
                totalRecruiters,
                totalJobs,
                activeJobs,
                totalApplications,
                totalResumes);
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