package com.devcv.resume.application;

import com.devcv.resume.domain.Resume;
import com.devcv.resume.domain.dto.PaginatedResumeResponse;
import com.devcv.resume.domain.dto.ResumeDto;
import com.devcv.resume.domain.dto.ResumeListResponse;
import com.devcv.resume.domain.dto.ResumeRequest;
import com.devcv.resume.domain.enumtype.CompanyType;
import com.devcv.resume.domain.enumtype.ResumeStatus;
import com.devcv.resume.domain.enumtype.StackType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@Transactional
public interface ResumeService {
    // 이력서 목록 조회
    PaginatedResumeResponse findResumes(StackType stackType, CompanyType companyType, int page, int size);
    // 이력서 상세 조회
    ResumeDto getResumeDetail(Long resumeId);
    // 회원별 이력서 조회
    ResumeListResponse findResumesByMemberId(Long memberId);
    //이력서 판매승인 요청
    Resume register(ResumeRequest resumeRequest, MultipartFile resumeFile, List<MultipartFile> images, Long memberId);
    // 이력서 판매내역 상세 조회
    ResumeDto getRegisterResumeDetail(Long memberId, Long resumeId);
    // 이력서 등록완료 요청
    Resume completeRegistration(Long memberId, Long resumeId);
    // 이력서 엔티티 가져오기
    Resume findByResumeId(Long resumeId);
    // 이력서 등록 수정
    ResumeDto modify(Long resumeId, Long memberId, ResumeDto resumeDto, MultipartFile resumeFile, List<MultipartFile> images);
    // 이력서 삭제
    Resume remove(Long resumeId, Long memberId);
}
