package com.devcv.review.application;


import com.devcv.member.domain.Member;
import com.devcv.resume.domain.Resume;
import com.devcv.order.domain.Order;
import com.devcv.review.domain.Review;
import com.devcv.review.domain.dto.CommentDto;
import com.devcv.review.domain.dto.PaginatedReviewResponse;
import com.devcv.review.domain.dto.ReviewDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.stream.Collectors;


@Service
public interface ReviewService {

    // 이력서의 모든 구매후기 조회
    PaginatedReviewResponse getListOfResume(Long resumeId, int page, int  size, String sort);

    // 이력서 구매후기 등록
    Review register(Long resumeId, Long memberId, ReviewDto resumeReviewDto);

    // 이력서 구매후기 수정
    Review modifyReview(Long memberId, Long resumeId, Long reviewId, ReviewDto reviewDto);

    // 이력서 구매후기 삭제
    void deleteReview(Long resumeId, Long memberId, Long reviewId);

    default Review dtoToEntity(ReviewDto resumeReviewDto, Resume resume, Member member,Order order) {

        Review resumeReview = Review.builder()
                .reviewId(resumeReviewDto.getReviewId())
                .resume(resume)
                .member(member)
                .order(order)
                .grade(resumeReviewDto.getGrade())
                .text(resumeReviewDto.getText())
                .reviewerNickname(member.getNickName())
                .sellerNickname(resume.getMember().getNickName())
                .build();

        return resumeReview;
    }

    default ReviewDto entityToDto(Review resumeReview) {

        ReviewDto resumeReviewDto = ReviewDto.builder()
                .reviewId(resumeReview.getReviewId())
                .resumeId(resumeReview.getResume().getResumeId())
                .memberId(resumeReview.getMember().getMemberId())
                .orderId(resumeReview.getOrder().getOrderId())
                .reviewerNickname(resumeReview.getMember().getNickName())
                .sellerNickname(resumeReview.getResume().getMember().getNickName())
                .sellerEmail(resumeReview.getResume().getMember().getEmail())
                .grade(resumeReview.getGrade())
                .text(resumeReview.getText())
                .createdDate(resumeReview.getCreatedDate())
                .updatedDate(resumeReview.getUpdatedDate())
                .commentDtoList(resumeReview.getCommentList() != null ? resumeReview.getCommentList().stream()
                        .map(CommentDto::from)
                        .collect(Collectors.toList()) : new ArrayList<>())
                .build();

        return resumeReviewDto;
    }







}
