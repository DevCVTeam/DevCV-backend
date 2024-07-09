package com.devcv.resume.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResumeSitemapDto {
    private Long resumeId;
    private LocalDateTime createdAt;
}