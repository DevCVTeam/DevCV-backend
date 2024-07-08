package com.devcv.review.application;


import com.devcv.member.domain.Member;
import com.devcv.review.domain.Review;
import com.devcv.review.domain.dto.CommentDto;
import org.springframework.stereotype.Service;

@Service
public interface CommentService {

    // 댓글 등록
    CommentDto addComment(Long resumeId, Member member, CommentDto commentDto);

    // 댓글 삭제
     void removeComment(Long commentId);

    void removeCommentsByReview(Review review);

}
