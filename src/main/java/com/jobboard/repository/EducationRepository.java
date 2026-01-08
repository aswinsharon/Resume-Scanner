package com.jobboard.repository;

import com.jobboard.domain.Education;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EducationRepository extends JpaRepository<Education, Long> {

    List<Education> findByCandidateId(Long candidateId);

    List<Education> findByCandidateIdOrderByStartDateDesc(Long candidateId);
}