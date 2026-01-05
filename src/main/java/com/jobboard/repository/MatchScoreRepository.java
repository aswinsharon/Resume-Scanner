package com.jobboard.repository;

import com.jobboard.domain.MatchScore;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatchScoreRepository extends JpaRepository<MatchScore, Long> {

    Optional<MatchScore> findByCandidateIdAndJobId(Long candidateId, Long jobId);

    @Query("SELECT ms FROM MatchScore ms WHERE ms.job.id = :jobId ORDER BY ms.totalScore DESC")
    Page<MatchScore> findByJobIdOrderByTotalScoreDesc(@Param("jobId") Long jobId, Pageable pageable);

    @Query("SELECT ms FROM MatchScore ms WHERE ms.candidate.id = :candidateId ORDER BY ms.totalScore DESC")
    Page<MatchScore> findByCandidateIdOrderByTotalScoreDesc(@Param("candidateId") Long candidateId, Pageable pageable);

    @Query("SELECT ms FROM MatchScore ms JOIN FETCH ms.candidate c JOIN FETCH c.user " +
            "WHERE ms.job.id = :jobId ORDER BY ms.totalScore DESC")
    List<MatchScore> findByJobIdWithCandidateDetails(@Param("jobId") Long jobId);
}