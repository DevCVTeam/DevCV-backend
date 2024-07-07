package com.devcv.resume.infrastructure;


import com.devcv.common.exception.ErrorCode;
import com.devcv.resume.domain.Resume;
import com.devcv.resume.domain.dto.ResumeDto;
import com.devcv.resume.domain.dto.ResumeRequest;
import com.devcv.resume.domain.enumtype.ResumeStatus;
import com.devcv.resume.exception.HttpMessageNotReadableException;
import com.devcv.resume.exception.ResumeNotExistException;
import com.devcv.resume.exception.ResumeStatusException;

public class ResumeValidator {

    // ResumeRequest 검증
    public static void validateResumeRequest(ResumeRequest resumeRequest) {
        if (resumeRequest.getPrice() == null || resumeRequest.getTitle() == null || resumeRequest.getContent() == null ||
                resumeRequest.getStack() == null || resumeRequest.getCategory() == null) {
            throw new HttpMessageNotReadableException(ErrorCode.EMPTY_VALUE_ERROR);
        }
    }

    // ResumeDto 검증
    public static void validateResumeDto(ResumeDto resumeDto) {
        if (resumeDto.getPrice() < 0 || resumeDto.getTitle() == null || resumeDto.getContent() == null ||
                resumeDto.getStack() == null || resumeDto.getCategory() == null) {
            throw new HttpMessageNotReadableException(ErrorCode.EMPTY_VALUE_ERROR);
        }
    }

    // 이력서 승인상태 확인
    public static void validateResumeForCompletion(Resume resume) {
        if (resume.getStatus() == ResumeStatus.pending || resume.getStatus() == ResumeStatus.modified) {
            throw new ResumeStatusException(ErrorCode.RESUME_NOT_APPROVAL);
        }
        if (resume.getStatus() == ResumeStatus.deleted) {
            throw new ResumeNotExistException(ErrorCode.RESUME_NOT_EXIST);
        }
    }

    // 이력서 수정상태 확인
    public static void validateResumeForModification(Resume resume, ResumeDto resumeDto) {
        if (resume.getStatus() == ResumeStatus.deleted) {
            throw new ResumeNotExistException(ErrorCode.RESUME_NOT_EXIST);
        }
        ResumeValidator.validateResumeDto(resumeDto);
    }
}