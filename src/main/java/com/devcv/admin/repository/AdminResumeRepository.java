package com.devcv.admin.repository;

import com.devcv.resume.domain.Resume;
import com.devcv.resume.domain.enumtype.ResumeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AdminResumeRepository extends JpaRepository<Resume, Long> {

    @EntityGraph(attributePaths = {"member", "category", "imageList"}, type = EntityGraph.EntityGraphType.FETCH)
    @Query("SELECT r FROM Resume r WHERE r.status = :status ORDER BY r.createdDate ASC")
    Page<Resume> findByStatus(ResumeStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"member", "category"}, type=EntityGraph.EntityGraphType.FETCH)
    @Query("SELECT r FROM Resume r JOIN FETCH r.imageList WHERE r.resumeId = ?1")
    Optional<Resume> findByResumeId(Long resumeId);

    @Modifying
    @Query("UPDATE Resume r set r.status = :status where r.resumeId = :resumeId")
    int updateByresumeId(Long resumeId, ResumeStatus status);
}
