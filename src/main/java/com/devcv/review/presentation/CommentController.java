package com.devcv.review.presentation;

import com.devcv.common.exception.ErrorCode;
import com.devcv.common.exception.InternalServerException;
import com.devcv.common.exception.UnAuthorizedException;
import com.devcv.member.application.MemberService;
import com.devcv.member.domain.Member;
import com.devcv.review.application.CommentService;
import com.devcv.review.domain.dto.CommentDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reviews")
@Slf4j
@RequiredArgsConstructor
public class CommentController {

    private final MemberService memberService;
    private final CommentService commentService;



    // ------------------판매자 코멘트 엔드포인트 start------------------------

    // 코멘트 등록
    @PostMapping("/{review-id}/comments")
    public ResponseEntity<CommentDto> addComment(@PathVariable("review-id") Long reviewId,
                                                 @AuthenticationPrincipal UserDetails userDetails, @RequestBody CommentDto commentDto) {

       Long memberId = validateUser(userDetails);
       Member member = memberService.findMemberBymemberId(memberId);

        CommentDto reviewComment = commentService.addComment(reviewId, member, commentDto);
        if (reviewComment != null) {
            return new ResponseEntity<>(reviewComment, HttpStatus.OK);
        } else {
            throw new InternalServerException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    public Long validateUser(UserDetails userDetails) {
        if (userDetails == null) {
            throw new UnAuthorizedException(ErrorCode.UNAUTHORIZED_ERROR);
        }
        return Long.valueOf(userDetails.getUsername());
    }

}
