package com.devcv.order.domain.dto;

import com.devcv.resume.domain.Resume;
import com.devcv.resume.domain.enumtype.StackType;

import java.time.LocalDateTime;

public record OrderResumeDto(Long resumeId, String title, StackType stackType, String sellerName,
                             String sellerNickName, String sellerEmail, int price, String thumbnailPath,
                             LocalDateTime createdDate, LocalDateTime updatedDate) {

    public static OrderResumeDto from(Resume resume) {
        return new OrderResumeDto(
                resume.getResumeId(),
                resume.getTitle(),
                resume.getCategory().getStackType(),
                resume.getMember().getMemberName(),
                resume.getMember().getNickName(),
                resume.getMember().getEmail(),
                resume.getPrice(),
                resume.getImageList().get(0).getResumeImgPath(),
                resume.getCreatedDate(),
                resume.getUpdatedDate());
    }
}
