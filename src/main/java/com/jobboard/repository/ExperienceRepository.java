package com.jobboard.repository;

import com.jobboard.domain.Experience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExperienceRepository extends JpaRepository<Experience, Long> {

    List<Experience> findByCandidateId(Long candidateId);

    List<Experience> findByCandidateIdOrderByStartDateDesc(Long candidateId);

    List<Experience> findByCandidateIdAndIsCurrentTrue(Long candidateId);
}