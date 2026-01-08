package com.jobboard.repository;

import com.jobboard.domain.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {

    List<Resume> findByCandidateId(Long candidateId);

    @Query("SELECT r FROM Resume r JOIN FETCH r.resumeSkills WHERE r.candidate.id = :candidateId")
    List<Resume> findByCandidateIdWithSkills(@Param("candidateId") Long candidateId);

    Optional<Resume> findFirstByCandidateIdOrderByCreatedAtDesc(Long candidateId);
}