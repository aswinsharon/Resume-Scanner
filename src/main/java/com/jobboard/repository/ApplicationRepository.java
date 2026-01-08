package com.jobboard.repository;

import com.jobboard.domain.Application;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    boolean existsByCandidateIdAndJobId(Long candidateId, Long jobId);

    Optional<Application> findByCandidateIdAndJobId(Long candidateId, Long jobId);

    Page<Application> findByCandidateIdOrderByAppliedAtDesc(Long candidateId, Pageable pageable);

    Page<Application> findByJobIdOrderByMatchScoreDesc(Long jobId, Pageable pageable);

    @Query("SELECT a FROM Application a JOIN FETCH a.candidate c JOIN FETCH c.user " +
            "WHERE a.job.id = :jobId ORDER BY a.matchScore DESC")
    List<Application> findByJobIdWithCandidateDetails(@Param("jobId") Long jobId);

    List<Application> findByJobIdAndStatus(Long jobId, Application.ApplicationStatus status);

    @Query("SELECT COUNT(a) FROM Application a WHERE a.job.recruiter.id = :recruiterId")
    long countByRecruiterId(@Param("recruiterId") Long recruiterId);
}