package com.devcv.review.application;

import com.devcv.common.exception.ErrorCode;
import com.devcv.common.exception.UnAuthorizedException;
import com.devcv.member.domain.Member;
import com.devcv.member.repository.MemberRepository;
import com.devcv.order.domain.Order;
import com.devcv.order.domain.OrderResume;
import com.devcv.order.exception.OrderNotFoundException;
import com.devcv.order.repository.OrderRepository;
import com.devcv.order.repository.OrderResumeRepository;
import com.devcv.resume.domain.Resume;
import com.devcv.resume.domain.enumtype.ResumeStatus;
import com.devcv.resume.exception.MemberNotFoundException;
import com.devcv.resume.exception.ResumeNotFoundException;
import com.devcv.resume.repository.ResumeRepository;
import com.devcv.review.domain.Comment;
import com.devcv.review.domain.Review;
import com.devcv.review.domain.dto.PaginatedReviewResponse;
import com.devcv.review.domain.dto.ReviewDto;
import com.devcv.review.exception.AlreadyExistsException;
import com.devcv.review.exception.ReviewNotFoundException;
import com.devcv.review.repository.CommentRepository;
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
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewServiceImpl implements  ReviewService{

    private final ReviewRepository reviewRepository;
    private final ResumeRepository resumeRepository;
    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;
    private final OrderResumeRepository orderResumeRepository;
    private final CommentRepository commentRepository;

    private final CommentService commentService;

    // 구매후기 조회
    @Transactional
    @Override
    public PaginatedReviewResponse getListOfResume(Long resumeId, int page, int size, String sort) {
        Pageable pageable;

        // 평점 높은순, 평점 낮은순, default 조회
        switch (sort) {
            case "r-desc":
                pageable = PageRequest.of(page-1, size, Sort.by("grade").descending());
                break;
            case "r-asc":
                pageable = PageRequest.of(page-1, size, Sort.by("grade").ascending());
                break;
            default:
                pageable = PageRequest.of(page-1, size, Sort.by("createdDate").descending());
                break;
        }

        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResumeNotFoundException(ErrorCode.RESUME_NOT_FOUND));
        if (resume.getStatus() != ResumeStatus.regcompleted) {
            throw new ResumeNotFoundException(ErrorCode.RESUME_NOT_FOUND);
        }

        Page<Review> reviewPage = reviewRepository.findByResume(resume, pageable);

        // 전체 리뷰 개수와 평균 별점 조회
        long totalReviews = reviewRepository.countByResumeId(resumeId);
        int averageRating = reviewRepository.calculateAverageGrade(resumeId);

        // 각 점수별 구매후기 개수 계산
        Long[] ratingCounts = new Long[5];
        Map<Integer, Long> numOfRating = reviewRepository.findByResume(resume, pageable).stream()
                .collect(Collectors.groupingBy(Review::getGrade, Collectors.counting()));

        for (int i = 0; i < 5; i++) {
            ratingCounts[i] = numOfRating.getOrDefault(i + 1, 0L);
        }


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
        List<OrderResume> orderResumes = orderResumeRepository.findByMemberIdAndResumeId(memberId, resumeId);
        if (orderResumes.isEmpty()) {
            throw new OrderNotFoundException(ErrorCode.ORDER_NOT_FOUND);
        }

        //  잘못된 접근 예외처리
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResumeNotFoundException(ErrorCode.RESUME_NOT_FOUND));
        if (resume.getStatus() != ResumeStatus.regcompleted) {
            throw new ResumeNotFoundException(ErrorCode.RESUME_NOT_FOUND);
        }
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(ErrorCode.MEMBER_NOT_FOUND));
        Order order = orderResumes.get(0).getOrder();
        Long orderId = order.getOrderId();
        resumeReviewDto.setResumeId(resumeId);
        resumeReviewDto.setMemberId(memberId);
        resumeReviewDto.setOrderId(orderId);

        // 구매후기 중복등록 확인
        if (reviewRepository.existsByResumeAndMember(resume, member)) {
            throw new AlreadyExistsException(ErrorCode.ALREADY_EXISTS);
        }

        Review resumeReview = dtoToEntity(resumeReviewDto, resume, member, order);

        reviewRepository.save(resumeReview);

        return resumeReview;
    }

    // 구매후기 수정
    @Transactional
    @Override
    public Review modifyReview(Long memberId, Long resumeId, Long reviewId, ReviewDto reviewDto) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(ErrorCode.REVIEW_NOT_FOUND));

        // 구매후기 수정 권한 확인
        if (!review.getMember().getMemberId().equals(memberId)) {
            throw new UnAuthorizedException(ErrorCode.UNAUTHORIZED_ERROR);
        }

        // 잘못된 접근 예외처리
        Optional<Review> reviewIdOpt = reviewRepository.findByResumeIdAndReviewId(resumeId, reviewId);
        if (reviewIdOpt.isEmpty()) {
            throw new ReviewNotFoundException(ErrorCode.REVIEW_NOT_FOUND);
        }

        review.changeText(reviewDto.getText());
        review.changeGrade(reviewDto.getGrade());

        return reviewRepository.save(review);
    }

    @Transactional
    @Override
    public void deleteReview(Long resumeId, Long memberId,  Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(ErrorCode.REVIEW_NOT_FOUND));

        // 구매후기 삭제 권한 확인
        if (!review.getMember().getMemberId().equals(memberId)) {
            throw new UnAuthorizedException(ErrorCode.UNAUTHORIZED_ERROR);
        }

        // 잘못된 접근 예외처리
        Optional<Review> reviewIdOpt = reviewRepository.findByResumeIdAndReviewId(resumeId, reviewId);
        if (reviewIdOpt.isEmpty()) {
            throw new ReviewNotFoundException(ErrorCode.REVIEW_NOT_FOUND);
        }

        // 판매자 댓글 삭제
        List<Comment> comments = commentRepository.findByReview(review);
        for (Comment comment : comments) {
            commentService.removeComment(comment.getCommentId());
        }

        reviewRepository.deleteById(reviewId);
    }







}
