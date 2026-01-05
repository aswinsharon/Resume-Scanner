package com.jobboard.repository;

import com.jobboard.domain.Recruiter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RecruiterRepository extends JpaRepository<Recruiter, Long> {

    Optional<Recruiter> findByUserId(Long userId);

    @Query("SELECT r FROM Recruiter r JOIN FETCH r.user WHERE r.id = :id")
    Optional<Recruiter> findByIdWithUser(@Param("id") Long id);
}