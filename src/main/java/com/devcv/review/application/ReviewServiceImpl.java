package com.devcv.review.application;

import com.devcv.common.exception.ErrorCode;
import com.devcv.member.domain.Member;
import com.devcv.order.domain.Order;
import com.devcv.order.domain.OrderResume;
import com.devcv.resume.domain.Resume;
import com.devcv.review.domain.Review;
import com.devcv.review.domain.dto.PaginatedReviewResponse;
import com.devcv.review.domain.dto.ReviewDto;
import com.devcv.review.exception.AlreadyExistsException;
import com.devcv.review.exception.ReviewNotFoundException;
import com.devcv.review.infrastructure.ReviewValidator;
import com.devcv.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewServiceImpl implements  ReviewService{

    private final ReviewRepository reviewRepository;
    private final CommentService commentService;
    private final ReviewValidator reviewValidator;

    // 구매후기 조회
    @Transactional
    @Override
    public PaginatedReviewResponse getListOfResume(Long resumeId, int page, int size, String sort) {

        Pageable pageable = getPageable(page, size, sort);

        Resume resume = reviewValidator.validateResume(resumeId);

        Page<Review> reviewPage = reviewRepository.findByResume(resume, pageable);

        // 전체 리뷰 개수와 평균 별점 조회
        long totalReviews = reviewRepository.countByResumeId(resumeId);
        int averageRating = reviewRepository.calculateAverageGrade(resumeId);

        Long[] ratingCounts = calculateRatingCounts(reviewRepository.findAllByResume(resume));

        List<ReviewDto> reviewDtos = reviewPage.getContent().stream().map(this::entityToDto).toList();
        return new PaginatedReviewResponse(
                reviewDtos,
                reviewPage.getTotalElements(),
                reviewPage.getNumberOfElements(),
                reviewPage.getNumber()+1, // currentPage
                reviewPage.getTotalPages(),
                reviewPage.getSize(),
                Math.max(1, reviewPage.getNumber() - 2),  // startPage
                Math.min(reviewPage.getTotalPages(), reviewPage.getNumber() + 2), // endPage
                totalReviews,
                averageRating,
                ratingCounts
        );


    }

    // 구매후기 등록
    @Transactional
    @Override
    public Review register(Long resumeId, Long memberId, ReviewDto resumeReviewDto) {

        // 주문여부 확인
        List<OrderResume> orderResumes = reviewValidator.validateOrderExists(memberId,resumeId);

        Resume resume = reviewValidator.validateResume(resumeId);
        Member member = reviewValidator.validateMember(memberId);

        // 구매후기 중복등록 확인
        if (reviewRepository.existsByResumeAndMember(resume, member)) {
            throw new AlreadyExistsException(ErrorCode.ALREADY_EXISTS);
        }

        Order order = orderResumes.get(0).getOrder();
        Long orderId = order.getOrderId();
        resumeReviewDto.setResumeId(resumeId);
        resumeReviewDto.setMemberId(memberId);
        resumeReviewDto.setOrderId(orderId);

        Review resumeReview = dtoToEntity(resumeReviewDto, resume, member, order);

        return reviewRepository.save(resumeReview);
    }

    // 구매후기 수정
    @Transactional
    @Override
    public Review modifyReview(Long memberId, Long resumeId, Long reviewId, ReviewDto reviewDto) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(ErrorCode.REVIEW_NOT_FOUND));

        reviewValidator.validateMemberAuthorization(memberId, review);
        reviewValidator.validateReview(resumeId, reviewId);

        review.changeText(reviewDto.getText());
        review.changeGrade(reviewDto.getGrade());

        return reviewRepository.save(review);
    }

    @Transactional
    @Override
    public void deleteReview(Long resumeId, Long memberId,  Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(ErrorCode.REVIEW_NOT_FOUND));

        reviewValidator.validateMemberAuthorization(memberId, review);
        reviewValidator.validateReview(resumeId, reviewId);

        commentService.removeCommentsByReview(review);
        reviewRepository.deleteById(reviewId);
    }

    private Pageable getPageable(int page, int size, String sort) {
        switch (sort) {
            case "r-desc":
                return PageRequest.of(page - 1, size, Sort.by("grade").descending());
            case "r-asc":
                return PageRequest.of(page - 1, size, Sort.by("grade").ascending());
            default:
                return PageRequest.of(page - 1, size, Sort.by("createdDate").descending());
        }
    }

    private Long[] calculateRatingCounts(List<Review> reviews) {
        Long[] ratingCounts = new Long[5];
        Map<Integer, Long> numOfRating = reviews.stream()
                .collect(Collectors.groupingBy(Review::getGrade, Collectors.counting()));

        for (int i = 0; i < 5; i++) {
            ratingCounts[i] = numOfRating.getOrDefault(i + 1, 0L);
        }
        return ratingCounts;
    }

}
