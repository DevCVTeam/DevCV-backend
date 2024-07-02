package com.devcv.order.domain.dto;

import com.devcv.resume.domain.Resume;

import java.time.LocalDateTime;
import java.util.List;

public record OrderSheetResume(String title, String sellerName, int price, List<String> stackType,
                               LocalDateTime updatedDate) {

    public static OrderSheetResume from(Resume resume) {
        return new OrderSheetResume(resume.getTitle(), resume.getMember().getNickName(), resume.getPrice(),
                resume.getStack(), resume.getUpdatedDate());
    }
}