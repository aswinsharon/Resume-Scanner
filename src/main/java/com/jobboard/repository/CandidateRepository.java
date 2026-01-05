package com.jobboard.repository;

import com.jobboard.domain.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long> {

    Optional<Candidate> findByUserId(Long userId);

    @Query("SELECT c FROM Candidate c JOIN FETCH c.user WHERE c.id = :id")
    Optional<Candidate> findByIdWithUser(@Param("id") Long id);

    @Query("SELECT c FROM Candidate c JOIN FETCH c.resumes WHERE c.id = :id")
    Optional<Candidate> findByIdWithResumes(@Param("id") Long id);
}