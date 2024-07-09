package com.devcv.review.repository;

import com.devcv.member.domain.Member;
import com.devcv.resume.domain.Resume;
import com.devcv.review.domain.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;


public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("SELECT COUNT(r) FROM Review r WHERE r.resume.resumeId = :resumeId")
    Long countByResumeId(Long resumeId);

    // 평균 별점 조회
    @Query("SELECT COALESCE(AVG(r.grade), 0) FROM Review r WHERE r.resume.resumeId = :resumeId")
    int calculateAverageGrade(Long resumeId);

    // 특정 이력서 모든 리뷰 확인
    @EntityGraph(attributePaths = {"member", "order"}, type=EntityGraph.EntityGraphType.FETCH)
    Page<Review> findByResume(Resume resume, Pageable pageable);

    List<Review> findAllByResume(Resume resume);

    // 특정 회원이 특정 이력서에 대해 이미 구매후기를 작성했는지 중복 확인
    @Query("SELECT COUNT(r) > 0 FROM Review r WHERE r.resume = :resume AND r.member = :member")
    boolean existsByResumeAndMember(Resume resume, Member member);

    @Query("SELECT r FROM Review r WHERE r.resume.resumeId = :resumeId AND r.reviewId = :reviewId")
    Optional<Review> findByResumeIdAndReviewId(Long resumeId, Long reviewId);

    // 구매후기 삭제
    void deleteById(Long reviewId);

}
