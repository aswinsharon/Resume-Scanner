package com.jobboard.repository;

import com.jobboard.domain.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    Page<Job> findByRecruiterId(Long recruiterId, Pageable pageable);

    Page<Job> findByStatus(Job.JobStatus status, Pageable pageable);

    @Query("SELECT j FROM Job j WHERE j.status = 'ACTIVE' AND " +
            "(:title IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
            "(:location IS NULL OR LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
            "(:jobType IS NULL OR j.jobType = :jobType) AND " +
            "(:remote IS NULL OR j.remote = :remote) AND " +
            "(:salaryMin IS NULL OR j.salaryMax >= :salaryMin)")
    Page<Job> searchJobs(@Param("title") String title,
            @Param("location") String location,
            @Param("jobType") Job.JobType jobType,
            @Param("remote") Boolean remote,
            @Param("salaryMin") BigDecimal salaryMin,
            Pageable pageable);

    @Query("SELECT j FROM Job j JOIN FETCH j.jobSkills WHERE j.id = :id")
    Optional<Job> findByIdWithSkills(@Param("id") Long id);

    @Query("SELECT j FROM Job j JOIN FETCH j.recruiter WHERE j.id = :id")
    Optional<Job> findByIdWithRecruiter(@Param("id") Long id);

    @Query("SELECT j FROM Job j WHERE j.status = 'ACTIVE' AND " +
            "EXISTS (SELECT js FROM JobSkill js WHERE js.job = j AND js.skillName IN :skills)")
    List<Job> findJobsBySkills(@Param("skills") List<String> skills);
}