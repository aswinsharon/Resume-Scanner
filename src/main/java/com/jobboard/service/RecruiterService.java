package com.jobboard.service;

import com.jobboard.domain.Recruiter;
import com.jobboard.domain.User;
import com.jobboard.dto.recruiter.RecruiterProfileRequest;
import com.jobboard.dto.recruiter.RecruiterProfileResponse;
import com.jobboard.dto.user.UserResponse;
import com.jobboard.exception.ResourceNotFoundException;
import com.jobboard.repository.RecruiterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@Transactional
public class RecruiterService {

    @Autowired
    private RecruiterRepository recruiterRepository;

    public RecruiterProfileResponse getProfile(Long userId) {
        Recruiter recruiter = recruiterRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Recruiter profile not found"));

        return convertToRecruiterProfileResponse(recruiter);
    }

    public RecruiterProfileResponse updateProfile(Long userId, RecruiterProfileRequest request) {
        Recruiter recruiter = recruiterRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Recruiter profile not found"));

        recruiter.setCompany(request.getCompany());
        recruiter.setDepartment(request.getDepartment());
        recruiter.setPhone(request.getPhone());

        Recruiter savedRecruiter = recruiterRepository.save(recruiter);
        return convertToRecruiterProfileResponse(savedRecruiter);
    }

    private RecruiterProfileResponse convertToRecruiterProfileResponse(Recruiter recruiter) {
        UserResponse userResponse = convertToUserResponse(recruiter.getUser());
        return new RecruiterProfileResponse(
                recruiter.getId(),
                userResponse,
                recruiter.getCompany(),
                recruiter.getDepartment(),
                recruiter.getPhone());
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