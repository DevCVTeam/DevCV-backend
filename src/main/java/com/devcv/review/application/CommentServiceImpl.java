package com.devcv.review.application;

import com.devcv.common.exception.ErrorCode;
import com.devcv.common.exception.UnAuthorizedException;
import com.devcv.member.domain.Member;
import com.devcv.review.domain.Comment;
import com.devcv.review.domain.Review;
import com.devcv.review.domain.dto.CommentDto;
import com.devcv.review.exception.AlreadyExistsException;
import com.devcv.review.exception.ReviewNotFoundException;
import com.devcv.review.infrastructure.ReviewValidator;
import com.devcv.review.repository.CommentRepository;
import com.devcv.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@Slf4j
@RequiredArgsConstructor
public class CommentServiceImpl implements  CommentService{

    private final CommentRepository commentRepository;
    private final ReviewValidator reviewValidator;

    @Transactional
    @Override
    public CommentDto addComment(Long reviewId, Member member, CommentDto commentDto) {
        Review review = reviewValidator.validateReview(reviewId);

        reviewValidator.validateCommenterAuthorization(member.getMemberId(), review);
        reviewValidator.validateCommentExists(review, member);

        Comment comment = Comment.builder()
                .review(review)
                .member(member)
                .text(commentDto.getText())
                .sellerNickname(member.getNickName())
                .build();

        review.addComment(comment);
        commentRepository.save(comment);
        return CommentDto.from(comment);

    }

    @Transactional
    public void removeCommentsByReview(Review review) {
        List<Comment> comments = commentRepository.findByReview(review);
        for (Comment comment : comments) {
            removeComment(comment.getCommentId());
        }
    }

    @Transactional
    @Override
    public void removeComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ReviewNotFoundException(ErrorCode.REVIEW_NOT_FOUND));

        reviewValidator.validateCommentAuthorization(comment.getMember(), comment);

        Review review = comment.getReview();
        review.removeComment(comment);
        commentRepository.delete(comment);
    }

}
