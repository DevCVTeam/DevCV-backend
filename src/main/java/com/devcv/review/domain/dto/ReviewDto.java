package com.devcv.review.domain.dto;

import com.devcv.review.domain.Review;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDto {

    private Long reviewId;

    private Long resumeId;

    private Long memberId;
    private String reviewerNickname;

    private Long orderId;

    private int grade;

    private String text;

    private LocalDateTime createdDate, updatedDate;

    private String sellerNickname;
    private String sellerEmail;

    private List<CommentDto> commentDtoList;

    public static ReviewDto from(Review review) {
        return ReviewDto.builder()
                .reviewId(review.getReviewId())
                .resumeId(review.getResume().getResumeId())
                .memberId(review.getMember().getMemberId())
                .orderId(review.getOrder().getOrderId())
                .reviewerNickname(review.getMember().getNickName())
                .sellerNickname(review.getResume().getMember().getNickName())
                .sellerEmail(review.getResume().getMember().getEmail())
                .grade(review.getGrade())
                .text(review.getText())
                .createdDate(review.getCreatedDate())
                .updatedDate(review.getUpdatedDate())
                .commentDtoList(review.getCommentList().stream()
                        .map(CommentDto::from)
                        .collect(Collectors.toList()))
                .build();
    }


}
