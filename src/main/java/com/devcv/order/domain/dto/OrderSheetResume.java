package com.devcv.order.domain.dto;

import com.devcv.resume.domain.Resume;
import com.devcv.resume.domain.enumtype.StackType;

import java.time.LocalDateTime;

public record OrderSheetResume(String title, String sellerName, int price, StackType stackType,
                               LocalDateTime updatedDate) {

    public static OrderSheetResume from(Resume resume) {
        return new OrderSheetResume(resume.getTitle(), resume.getMember().getNickName(), resume.getPrice(),
                resume.getCategory().getStackType(), resume.getUpdatedDate());
    }
}