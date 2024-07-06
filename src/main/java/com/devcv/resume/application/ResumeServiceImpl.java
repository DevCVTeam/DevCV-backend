package com.devcv.resume.application;

import com.devcv.common.exception.ErrorCode;
import com.devcv.common.exception.UnAuthorizedException;
import com.devcv.member.domain.Member;
import com.devcv.member.repository.MemberRepository;
import com.devcv.resume.domain.Category;
import com.devcv.resume.domain.Resume;
import com.devcv.resume.domain.ResumeImage;
import com.devcv.resume.domain.ResumeLog;
import com.devcv.resume.domain.dto.*;
import com.devcv.resume.domain.enumtype.CompanyType;
import com.devcv.resume.domain.enumtype.ResumeStatus;
import com.devcv.resume.domain.enumtype.StackType;
import com.devcv.resume.exception.*;
import com.devcv.resume.infrastructure.S3Uploader;
import com.devcv.resume.repository.CategoryRepository;
import com.devcv.resume.repository.ResumeLogRepository;
import com.devcv.resume.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.devcv.resume.domain.dto.ResumeDto.entityToDto;


@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ResumeServiceImpl implements ResumeService {

    private final ResumeRepository resumeRepository;
    private final CategoryRepository categoryRepository;
    private final ResumeLogRepository resumeLogRepository;
    private final MemberRepository memberRepository;
    private final S3Uploader s3Uploader;

    private void saveResumeLog(Resume resume, ResumeStatus status) {
        ResumeLog history = ResumeLog.builder()
                .resumeId(resume.getResumeId())
                .title(resume.getTitle())
                .status(status)
                .build();
        resumeLogRepository.save(history);
    }


    // 이력서 목록 조회
    @Override
    public PaginatedResumeResponse findResumes(StackType stackType, CompanyType companyType, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Object[]> resumePage;

        if (stackType != null && companyType != null) {
            // 직무 & 회사별 목록 조회
            resumePage = resumeRepository.findApprovedResumesByStackTypeAndCompanyType(stackType, companyType, pageable);
        } else if (stackType != null) {
            // 직무별 목록 조회
            resumePage = resumeRepository.findApprovedResumesByStackType(stackType, pageable);
        } else if (companyType != null) {
            // 회사별 목록 조회
            resumePage = resumeRepository.findApprovedResumesByCompanyType(companyType, pageable);
        } else {
            // 전체 목록 조회
            resumePage = resumeRepository.findApprovedResumes(pageable);
        }

        List<ResumeDto> resumeDTOs = resumePage.getContent()
                .stream()
                .map(objects -> {
                    Resume resume = (Resume) objects[0];
                    Double averageGrade = (Double) objects[1];
                    Long reviewCount = (Long) objects[2];
                    return ResumeDto.entityToDto(resume, averageGrade, reviewCount, false);
                })
                .collect(Collectors.toList());

        int currentPage = resumePage.getNumber() + 1;
        int totalPages = resumePage.getTotalPages();
        int startPage = Math.max(1, currentPage - 2);
        int endPage = Math.min(totalPages, currentPage + 2);

        return new PaginatedResumeResponse(
                resumeDTOs,
                resumePage.getTotalElements(),
                resumePage.getNumberOfElements(),
                currentPage,
                totalPages,
                resumePage.getSize(),
                startPage,
                endPage
        );
    }

    // 이력서 상세 조회
    @Override
    public ResumeDto getResumeDetail(Long resumeId) {
        List<Object[]>result = resumeRepository.findByIdAndStatus(resumeId);

        if (result.isEmpty() || result.get(0) == null || result.get(0)[0] == null) {
            throw new ResumeNotFoundException(ErrorCode.RESUME_NOT_FOUND);
        }

        Resume resume = (Resume) result.get(0)[0];

        Double averageGrade = (Double) result.get(0)[1];
        Long reviewCount = (Long) result.get(0)[2];

        return entityToDto(resume, averageGrade, reviewCount, false);
    }

    // 이력서 등록(승인대기)
    @Override
    public Resume register(ResumeRequest resumeRequest, MultipartFile resumeFile, List<MultipartFile> images, Long memberId) {

        Member member = memberRepository.findMemberBymemberId(memberId);

        if (member == null) {
            throw new MemberNotFoundException(ErrorCode.MEMBER_NOT_FOUND);
        }

        // Category 저장
        CategoryDto categoryDTO = resumeRequest.getCategory();
        List<Category> categories = categoryRepository.findByCompanyTypeAndStackType(
                categoryDTO.getCompanyType(),
                categoryDTO.getStackType()
        );
        Category category;
        if (categories.isEmpty()) {
            // 리스트가 비어 있으면 새로운 Category 생성 및 저장
            category = new Category(categoryDTO.getCompanyType(), categoryDTO.getStackType());
            category = categoryRepository.save(category);
        } else {
            // 리스트가 비어 있지 않으면 첫 번째 요소 사용
            category = categories.get(0);
        }

        // PDF 파일 업로드
        String resumeFilePath = null;
        if (resumeFile != null) {
            resumeFilePath = s3Uploader.upload(resumeFile);
            log.debug("Uploaded resume file path: {}", resumeFilePath);
        }

        if (resumeRequest.getPrice() == null || resumeRequest.getTitle() == null || resumeRequest.getContent() == null ||
                resumeRequest.getStack() == null || resumeRequest.getCategory() == null) {
            throw new HttpMessageNotReadableException(ErrorCode.EMPTY_VALUE_ERROR);
        }

        Resume resume = Resume.builder()
                .member(member)
                .price(resumeRequest.getPrice())
                .title(resumeRequest.getTitle())
                .content(resumeRequest.getContent())
                .resumeFilePath(resumeFilePath)
                .stack(resumeRequest.getStack())
                .category(category)
                .build();

        // 이미지 파일 업로드
        if (images != null && !images.isEmpty()) {
            for (int i = 0; i < images.size(); i++) {
                String imagePath = s3Uploader.upload(images.get(i));
                ResumeImage resumeImage = ResumeImage.builder()
                        .resumeImgPath(imagePath)
                        .ord(i)
                        .build();

                resume.addImage(resumeImage);
            }
        }

         //상태 설정
         resume.setStatus(ResumeStatus.pending);
         saveResumeLog(resume, ResumeStatus.pending);

        return resumeRepository.save(resume);

    }

    // 회원별 이력서 조회
    @Override
    public ResumeListResponse findResumesByMemberId(Long memberId) {
        List<ResumeResponse> resumeList = resumeRepository.findByMember(memberId).
                stream()
                .map(ResumeResponse::from)
                .collect(Collectors.toList());;
        return ResumeListResponse.of(memberId,resumeList.size(),resumeList);
    }

    // 이력서 판매내역 상세조회
    @Override
    public ResumeDto getRegisterResumeDetail(Long memberId, Long resumeId) {
        Optional<Resume> resumeOpt = resumeRepository.findByIdAndMemberId(resumeId, memberId);
        if (resumeOpt.isPresent()) {
            Resume resume = resumeOpt.get();
            return ResumeDto.from(resume);
        }else {
            throw new ResumeNotFoundException(ErrorCode.RESUME_NOT_FOUND);
        }
    }

    // 이력서 최종 판매등록
    @Override
    public Resume completeRegistration( Long memberId, Long resumeId) {
        Member member = memberRepository.findMemberBymemberId(memberId);
        if (member == null) {
            throw new MemberNotFoundException(ErrorCode.MEMBER_NOT_FOUND);
        }
        Optional<Resume> resumeOpt = resumeRepository.findByIdAndMemberId(resumeId, memberId);
        if (resumeOpt.isPresent()) {
            Resume resume = resumeOpt.get();
            if (resume.getStatus() == ResumeStatus.pending || resume.getStatus() == ResumeStatus.modified) {
                throw new ResumeStatusException(ErrorCode.RESUME_NOT_APPROVAL);
            }
            if (resume.getStatus() == ResumeStatus.deleted) {
                throw new ResumeNotExistException(ErrorCode.RESUME_NOT_EXIST);
            }
            resume.setStatus(ResumeStatus.regcompleted);
            saveResumeLog(resume, ResumeStatus.regcompleted);
            return resumeRepository.save(resume);
        } else {
            throw new ResumeNotFoundException(ErrorCode.RESUME_NOT_FOUND);
        }
    }

    @Override
    public Resume findByResumeId(Long resumeId) {
        return resumeRepository.findByResumeId(resumeId)
                .orElseThrow(() -> new ResumeNotFoundException(ErrorCode.RESUME_NOT_FOUND));
    }


    // 이력서 등록 수정 요청
    @Override
    public ResumeDto modify(Long resumeId, Long memberId,
                            ResumeDto resumeDto, MultipartFile resumeFile, List<MultipartFile> images) {

        Member member = memberRepository.findMemberBymemberId(memberId);
        if (member == null) {
            throw new MemberNotFoundException(ErrorCode.MEMBER_NOT_FOUND);
        }
        // 조회
        Optional<Resume> resumeOpt = resumeRepository.findByResumeId(resumeId);

        if (resumeOpt.isPresent()) {
            Resume resume = resumeOpt.get();

            if (resume.getStatus() == ResumeStatus.deleted) {
                throw new ResumeNotExistException(ErrorCode.RESUME_NOT_EXIST);
            }

            if (resumeDto.getPrice() < 0 || resumeDto.getTitle() == null || resumeDto.getContent() == null ||
                    resumeDto.getStack() == null || resumeDto.getCategory() == null) {
                throw new HttpMessageNotReadableException(ErrorCode.EMPTY_VALUE_ERROR);
            }
            resume.changeTitle(resumeDto.getTitle());
            resume.changeContent(resumeDto.getContent());
            resume.changePrice(resumeDto.getPrice());
            resume.changeStack(resumeDto.getStack());
            resume.setStatus(ResumeStatus.modified);


            // 현재 상태를 로그로 저장
            saveResumeLog(resume, resume.getStatus());

            // Category 저장
            CategoryDto categoryDto = resumeDto.getCategory();
            List<Category> categories = categoryRepository.findByCompanyTypeAndStackType(
                    categoryDto.getCompanyType(),
                    categoryDto.getStackType()
            );

            Category category;
            if (categories.isEmpty()) {
                // 리스트가 비어 있으면 새로운 Category 생성 및 저장
                category = new Category( categoryDto.getCompanyType(),categoryDto.getStackType());
                category = categoryRepository.save(category);
            } else {
                // 리스트가 비어 있지 않으면 첫 번째 요소 사용
                category = categories.get(0);
            }
            resume.changeCategory(category);


            // 새로운 PDF 파일 업로드 및 기존 파일 경로 변경
            if (resumeFile != null && !resumeFile.isEmpty()) {
                String resumeFilePath = s3Uploader.upload(resumeFile);
                resume.changeResumeFilePath(resumeFilePath);
            }

            // 기존 이미지 리스트 초기화 및 새로운 이미지 업로드
            resume.clearList();
            if (images != null && !images.isEmpty()) {
                for (int i = 0; i < images.size(); i++) {
                    String imagePath = s3Uploader.upload(images.get(i));
                    ResumeImage resumeImage = ResumeImage.builder()
                            .resumeImgPath(imagePath)
                            .ord(i)
                            .build();
                    resume.addImage(resumeImage);
                }
            }

            // 기존 이미지 리스트 초기화 및 새로운 이미지 업로드
            resume.clearList();
            if (images != null && !images.isEmpty()) {
                for (int i = 0; i < images.size(); i++) {
                    String imagePath = s3Uploader.upload(images.get(i));
                    ResumeImage resumeImage = ResumeImage.builder()
                            .resumeImgPath(imagePath)
                            .ord(i)
                            .build();
                    resume.addImage(resumeImage);
                }
            }
            // 이력서 저장
            resumeRepository.save(resume);
            // 수정된 이력서를 ResumeDto로 변환하여 반환
            return ResumeDto.from(resume);
        } else {
            throw new ResumeNotFoundException(ErrorCode.RESUME_NOT_FOUND);
        }

    }

    // 이력서 삭제 요청
    @Override
    public Resume remove(Long resumeId,Long memberId) {

        Member member = memberRepository.findMemberBymemberId(memberId);
        if (member == null) {
            throw new MemberNotFoundException(ErrorCode.MEMBER_NOT_FOUND);
        }

        Optional<Resume> resumeOpt = resumeRepository.findById(resumeId);

        if (resumeOpt.isPresent()) {
            Resume resume = resumeOpt.get();
            resume.setStatus(ResumeStatus.deleted);
            if (!resume.getMember().getMemberId().equals(memberId)) {
                log.info("MemberId: " + resume.getMember().getMemberId());
                throw new UnAuthorizedException(ErrorCode.UNAUTHORIZED_ERROR);
            }
             //추후 관리자 쪽에서 delFlag 수정
            resumeRepository.updateToDelete(resumeId, true);
            saveResumeLog(resume, ResumeStatus.deleted);
            return resumeRepository.save(resume);
        } else {
            throw new ResumeNotFoundException(ErrorCode.RESUME_NOT_FOUND);
        }
    }
}
