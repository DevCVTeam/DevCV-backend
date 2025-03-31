package com.devcv.resume.domain.dto;

import com.devcv.resume.domain.Resume;
import com.devcv.resume.domain.enumtype.CompanyType;
import com.devcv.resume.domain.enumtype.ResumeStatus;
import com.devcv.resume.domain.enumtype.StackType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ResumeResponse {

    private Long resumeId;
    private String title;
    private int price;
    private String resumeFilePath;
    private String thumbnail;
    private String sellerNickname;
    private StackType stackType;
    private CompanyType companyType;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private ResumeStatus resumeStatus;


    public static ResumeResponse from(Resume resume) {
        return new ResumeResponse(
                resume.getResumeId(),
                resume.getTitle(),
                resume.getPrice(),
                resume.getResumeFilePath(),
                getThumbnailFromResume(resume), // 썸네일 이미지 직접 가져오기
                resume.getMember().getNickName(),
                resume.getCategory().getStackType(),
                resume.getCategory().getCompanyType(),
                resume.getCreatedDate(),
                resume.getUpdatedDate(),
                resume.getStatus()
        );
    }
    private static String getThumbnailFromResume(Resume resume) {
        if (resume.getImageList() != null && !resume.getImageList().isEmpty()) {
            return resume.getImageList().get(0).getResumeImgPath(); // 0번째 이미지 가져오기
        }
        return null;
    }

    public ResumeResponse withResumeFilePath(String resumeFilePath) {
        this.resumeFilePath = resumeFilePath;
        return this;
    }

}
