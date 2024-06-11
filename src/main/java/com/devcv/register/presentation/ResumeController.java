package com.devcv.register.presentation;

import com.devcv.member.domain.dto.MemberResponse;
import com.devcv.register.application.ResumeService;
import com.devcv.register.domain.Resume;
import com.devcv.register.domain.dto.ResumeDTO;
import com.devcv.register.domain.dto.ResumeRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

// security 설정 이후 공통 파라미터 변경, //2 삭제
//            @AuthenticationPrincipal CustomUserDetails userDetails,

// 인증된 사용자 정보 조회 통한 memberResponse get 로직 츠기
//        Long userId = userDetails.getUserId();
//        MemberResponse memberResponse = resumeService.getMemberResponse(userId);

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/resumes")
public class ResumeController {

    private final ResumeService resumeService;

    //------이력서 등록 페이지 호출 start --------
    @GetMapping("/new")
    public ResponseEntity<?> newResumePage(
            @RequestParam("userId") Long userId ) { //2

        // 회원정보 조회 후 에러 처리를 위한 임시 메서드->추후 security 설정 이후 변경
        MemberResponse memberResponse = resumeService.getMemberResponse(userId);

        Resume resume = resumeService.findRegisteredResumeByMember(userId);

        if (resume != null) {
            // 승인 대기 중인 이력서가 있는 경우 해당 이력서 반환
            return ResponseEntity.ok(ResumeDTO.from(resume));
        } else {
            // 승인 대기 중인 이력서가 없는 경우 새로운 이력서 페이지 반환
            return ResponseEntity.ok().body("새로운 이력서 페이지");
        }
    }
    //-------이력서 등록 페이지 호출 end -----------


    // -------이력서 승인대기 요청 start-------------
    @PostMapping("/add")
    public ResponseEntity<Resume> registerResume(
            @RequestPart("member") MemberResponse memberResponse, // 2
            @RequestPart("resume") ResumeRequest resumeRequest,
            @RequestPart("resumeFile") MultipartFile resumeFile,
            @RequestPart("images") List<MultipartFile> images) {

        // 파일들 DTO 설정
        resumeRequest.setResumeFile(resumeFile);
        resumeRequest.setImageFiles(images);


        Resume createdResume = resumeService.register(memberResponse, resumeRequest);

        return ResponseEntity.ok(createdResume);
    }

    //----------이력서 승인대기 요청 end---------------



    //----------이력서 판매등록 요청 start----------------
    @PostMapping("/complete")
    public ResponseEntity<?> completeResumeRegistration(
            @RequestPart("member") MemberResponse memberResponse, // 2
            @RequestPart("resumeId") Long resumeId) {

        Resume completedResume = resumeService.completeRegistration(memberResponse, resumeId);
        return ResponseEntity.ok(ResumeDTO.from(completedResume));
    }
    //----------이력서 판매등록 요청 end----------------



}

