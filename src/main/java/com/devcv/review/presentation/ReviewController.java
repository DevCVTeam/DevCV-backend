package com.devcv.review.presentation;

import com.devcv.common.exception.ErrorCode;
import com.devcv.common.exception.InternalServerException;
import com.devcv.common.exception.UnAuthorizedException;
import com.devcv.resume.exception.ResumeNotFoundException;
import com.devcv.review.application.ReviewService;
import com.devcv.review.domain.Review;
import com.devcv.review.domain.dto.PaginatedReviewResponse;
import com.devcv.review.domain.dto.ReviewDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/resumes")
@Slf4j
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;


    // ----------------------구매후기 전체 조회 start-----------------------
    @GetMapping("/{resume-id}/reviews")
    public ResponseEntity<PaginatedReviewResponse> getList(
            @RequestParam("page") int page, @RequestParam("size") int size,
            @RequestParam(value = "sort", defaultValue = "d-desc") String sort,
            @PathVariable("resume-id") Long resumeId) {

        try {
            PaginatedReviewResponse response = reviewService.getListOfResume(resumeId, page, size, sort);
            return ResponseEntity.ok(response);
        } catch (InternalServerException e) {
            throw new InternalServerException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

    }
    // ----------------------구매후기 전체 조회 end-----------------------


    // ----------------------구매후기 등록 start-------------------------
    @PostMapping("/{resume-id}/reviews")
    public ResponseEntity<ReviewDto> addReview(@PathVariable("resume-id") Long resumeId,
                                            @AuthenticationPrincipal UserDetails userDetails,
                                            @RequestBody ReviewDto resumeReviewDto) {

        Long memberId = validateUser(userDetails);

        Review resumeReview = reviewService.register(resumeId, memberId, resumeReviewDto);

        ReviewDto responseDto = reviewService.entityToDto(resumeReview);

        return new ResponseEntity<>(responseDto, HttpStatus.OK);

    }

    // ----------------------구매후기 등록 end --------------------------

    // ----------------------구매후기 수정 start --------------------------

    @PutMapping("/{resume-id}/reviews/{review-id}")
    public ResponseEntity<ReviewDto> updateReview(
            @PathVariable("resume-id") Long resumeId,
            @PathVariable("review-id") Long reviewId,
            @RequestBody ReviewDto reviewDto,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long memberId = validateUser(userDetails);

        Review modifiedReview = reviewService.modifyReview(memberId, resumeId, reviewId, reviewDto);
        if (modifiedReview != null) {
            ReviewDto responseDto = reviewService.entityToDto(modifiedReview);
            return new ResponseEntity<>(responseDto, HttpStatus.OK);
        } else {
            throw new InternalServerException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

    }

    // ----------------------구매후기 수정 end  --------------------------


    // ----------------------구매후기 삭제 start --------------------------

    @DeleteMapping("/{resume-id}/reviews/{review-id}")
    public Map<String, Object> deleteReview(
            @PathVariable("resume-id") Long resumeId,
            @PathVariable("review-id") Long reviewId,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long memberId =validateUser(userDetails);
        reviewService.deleteReview(resumeId, memberId, reviewId);

        if (reviewId != null) {
            return Map.of("deleted reviewId", reviewId);
        } else {
            throw new InternalServerException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
    // ----------------------구매후기 삭제 end  --------------------------

    public Long validateUser(UserDetails userDetails) {
        if (userDetails == null) {
            throw new UnAuthorizedException(ErrorCode.UNAUTHORIZED_ERROR);
        }
        return Long.valueOf(userDetails.getUsername());
    }

}
