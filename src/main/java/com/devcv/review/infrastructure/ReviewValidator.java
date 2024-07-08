package com.devcv.review.infrastructure;

import com.devcv.common.exception.ErrorCode;
import com.devcv.common.exception.UnAuthorizedException;
import com.devcv.member.domain.Member;
import com.devcv.member.repository.MemberRepository;
import com.devcv.order.domain.OrderResume;
import com.devcv.order.exception.OrderNotFoundException;
import com.devcv.order.repository.OrderResumeRepository;
import com.devcv.resume.domain.Resume;
import com.devcv.resume.domain.enumtype.ResumeStatus;
import com.devcv.resume.exception.MemberNotFoundException;
import com.devcv.resume.exception.ResumeNotFoundException;
import com.devcv.resume.repository.ResumeRepository;
import com.devcv.review.domain.Comment;
import com.devcv.review.domain.Review;
import com.devcv.review.exception.AlreadyExistsException;
import com.devcv.review.exception.ReviewNotFoundException;
import com.devcv.review.repository.CommentRepository;
import com.devcv.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ReviewValidator {

    private final ResumeRepository resumeRepository;
    private final MemberRepository memberRepository;
    private final ReviewRepository reviewRepository;
    private final CommentRepository commentRepository;
    private final OrderResumeRepository orderResumeRepository;


    public  Resume validateResume(Long resumeId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResumeNotFoundException(ErrorCode.RESUME_NOT_FOUND));
        if (resume.getStatus() != ResumeStatus.regcompleted) {
            throw new ResumeNotFoundException(ErrorCode.RESUME_NOT_FOUND);
        }
        return resume;
    }

    public Member validateMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(ErrorCode.MEMBER_NOT_FOUND));
    }

    public void validateMemberAuthorization(Long memberId, Review review) {
        if (!review.getMember().getMemberId().equals(memberId)) {
            throw new UnAuthorizedException(ErrorCode.UNAUTHORIZED_ERROR);
        }
    }

    public void validateCommenterAuthorization(Long memberId, Review review) {
        if (!review.getResume().getMember().getMemberId().equals(memberId)) {
            throw new UnAuthorizedException(ErrorCode.UNAUTHORIZED_ERROR);
        }
    }

    public void validateReview(Long resumeId, Long reviewId) {
        if (reviewRepository.findByResumeIdAndReviewId(resumeId, reviewId).isEmpty()) {
            throw new ReviewNotFoundException(ErrorCode.REVIEW_NOT_FOUND);
        }
    }

    public Review validateReview(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(ErrorCode.REVIEW_NOT_FOUND));
    }

    public void validateCommentExists(Review review, Member member) {
        if (commentRepository.existsByReviewAndMember(review, member)) {
            throw new AlreadyExistsException(ErrorCode.ALREADY_EXISTS);
        }
    }

    public void validateCommentAuthorization(Member member, Comment comment) {
        if (!comment.getMember().getMemberId().equals(member.getMemberId())) {
            throw new UnAuthorizedException(ErrorCode.UNAUTHORIZED_ERROR);
        }
    }

    public List<OrderResume> validateOrderExists(Long memberId, Long resumeId) {
        List<OrderResume> orderResumes = orderResumeRepository.findByMemberIdAndResumeId(memberId, resumeId);
        if (orderResumes.isEmpty()) {
            throw new OrderNotFoundException(ErrorCode.ORDER_NOT_FOUND);
        }
        return orderResumes;
    }



}
