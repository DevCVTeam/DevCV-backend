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
import com.devcv.resume.infrastructure.CategoryUtil;
import com.devcv.resume.infrastructure.ResumeValidator;
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


    // 이력서 목록 조회
    @Override
    public PaginatedResumeResponse findResumes(StackType stackType, CompanyType companyType, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Object[]> resumePage = fetchResumePage(stackType, companyType, pageable);
        List<ResumeDto> resumeDTOs = convertToResumeDtoList(resumePage);
        return createPaginatedResumeResponse(resumePage, resumeDTOs);
    }

    @Override
    public List<ResumeSitemapDto> getResumeIdsAndCreationDates() {
        List<Resume> resumes = resumeRepository.findAllByStatus(ResumeStatus.regcompleted);
        return resumes.stream()
                .map(resume -> new ResumeSitemapDto(resume.getResumeId(), resume.getCreatedDate()))
                .collect(Collectors.toList());
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

        Member member = findMemberById(memberId);
        // Category 저장 또는 가져오기
        Category category = CategoryUtil.getOrCreateCategory(categoryRepository, resumeRequest.getCategory());
        // PDF 파일 업로드
        String resumeFilePath = uploadResumeFile(resumeFile);
        // 이력서 요청 검증
        ResumeValidator.validateResumeRequest(resumeRequest);
        // 이력서 생성
        Resume resume = createResume(resumeRequest, member, category, resumeFilePath);
        // 이미지 파일 업로드
        uploadResumeImages(images, resume);
        return resumeRepository.save(resume);

    }

    // 회원별 이력서 조회
    @Override
    public ResumeListResponse findResumesByMemberId(Long memberId) {
        Member member = findMemberById(memberId);
        List<ResumeResponse> resumeList = resumeRepository.findByMember(member.getMemberId()).
                stream()
                .map(ResumeResponse::from)
                .collect(Collectors.toList());;
        return ResumeListResponse.of(memberId,resumeList.size(),resumeList);
    }

    // 이력서 판매내역 상세조회
    @Override
    public ResumeDto getRegisterResumeDetail(Long memberId, Long resumeId) {
        Member member = findMemberById(memberId);
        Optional<Resume> resumeOpt = resumeRepository.findByIdAndMemberId(resumeId, member.getMemberId());
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
        Member member = findMemberById(memberId);
        Optional<Resume> resumeOpt = resumeRepository.findByIdAndMemberId(resumeId, member.getMemberId());
        if (resumeOpt.isPresent()) {
            Resume resume = resumeOpt.get();
            ResumeValidator. validateResumeForCompletion(resume);

            resume.setStatus(ResumeStatus.regcompleted);
            saveResumeLog(resume, ResumeStatus.regcompleted);
            return resumeRepository.save(resume);
        } else {
            throw new ResumeNotFoundException(ErrorCode.RESUME_NOT_FOUND);
        }
    }


    // 이력서 등록 수정 요청
    @Override
    public ResumeDto modify(Long resumeId, Long memberId,
                            ResumeDto resumeDto, MultipartFile resumeFile, List<MultipartFile> images) {

        Member member = findMemberById(memberId);

        // 조회
        Optional<Resume> resumeOpt = resumeRepository.findByIdAndMemberId(resumeId, member.getMemberId());
        if (resumeOpt.isPresent()) {
            Resume resume = resumeOpt.get();
            ResumeValidator.validateResumeForModification(resume, resumeDto);
            updateResumeDetails(resume, resumeDto, resumeFile, images);
            resumeRepository.save(resume);
            return ResumeDto.from(resume);
        } else {
            throw new ResumeNotFoundException(ErrorCode.RESUME_NOT_FOUND);
        }

    }


    // 이력서 삭제 요청
    @Override
    public Resume remove(Long resumeId,Long memberId) {

        Member member = findMemberById(memberId);

        Optional<Resume> resumeOpt = resumeRepository.findByIdAndMemberId(resumeId, member.getMemberId());
        if (resumeOpt.isPresent()) {
            Resume resume = resumeOpt.get();
            if (!resume.getMember().getMemberId().equals(memberId)) {
                throw new UnAuthorizedException(ErrorCode.UNAUTHORIZED_ERROR);
            }
            resume.setStatus(ResumeStatus.deleted);
            resumeRepository.updateToDelete(resumeId, true);
            saveResumeLog(resume, ResumeStatus.deleted);
            return resumeRepository.save(resume);
        } else {
            throw new ResumeNotFoundException(ErrorCode.RESUME_NOT_FOUND);
        }
    }



    // 조건 검색 조회 메서드
    private Page<Object[]> fetchResumePage(StackType stackType, CompanyType companyType, Pageable pageable) {
        if (stackType != null && companyType != null) {
            return resumeRepository.findApprovedResumesByStackTypeAndCompanyType(stackType, companyType, pageable);
        } else if (stackType != null) {
            return resumeRepository.findApprovedResumesByStackType(stackType, pageable);
        } else if (companyType != null) {
            return resumeRepository.findApprovedResumesByCompanyType(companyType, pageable);
        } else {
            return resumeRepository.findApprovedResumes(pageable);
        }
    }


    // dto 리스트 변환 메서드
    private List<ResumeDto> convertToResumeDtoList(Page<Object[]> resumePage) {
        return resumePage.getContent()
                .stream()
                .map(objects -> {
                    Resume resume = (Resume) objects[0];
                    Double averageGrade = (Double) objects[1];
                    Long reviewCount = (Long) objects[2];
                    return entityToDto(resume, averageGrade, reviewCount, false);
                })
                .collect(Collectors.toList());
    }


    // 페이지네이션 생성 메서드
    private PaginatedResumeResponse createPaginatedResumeResponse(Page<Object[]> resumePage, List<ResumeDto> resumeDTOs) {
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


    // 회원 존재 여부 검증
    private Member findMemberById(Long memberId) {
        return Optional.ofNullable(memberRepository.findMemberBymemberId(memberId))
                .orElseThrow(() -> new MemberNotFoundException(ErrorCode.MEMBER_NOT_FOUND));
    }


    // 이력서 파일 업로드 메서드
    private String uploadResumeFile(MultipartFile resumeFile) {
        if (resumeFile != null) {
            String resumeFilePath = s3Uploader.upload(resumeFile);
            log.debug("Uploaded resume file path: {}", resumeFilePath);
            return resumeFilePath;
        }
        return null;
    }

    // 이미지 업로드 메서드
    private void uploadResumeImages(List<MultipartFile> images, Resume resume) {
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
    }


    // 이력서 생성 메서드
    private Resume createResume(ResumeRequest resumeRequest, Member member, Category category, String resumeFilePath) {
        Resume resume = Resume.builder()
                .member(member)
                .price(resumeRequest.getPrice())
                .title(resumeRequest.getTitle())
                .content(resumeRequest.getContent())
                .resumeFilePath(resumeFilePath)
                .stack(resumeRequest.getStack())
                .category(category)
                .build();

        resume.setStatus(ResumeStatus.pending);
        saveResumeLog(resume, ResumeStatus.pending);
        return resume;
    }

    // 이력서 수정 메서드
    private void updateResumeDetails(Resume resume, ResumeDto resumeDto, MultipartFile resumeFile, List<MultipartFile> images) {
        resume.changeTitle(resumeDto.getTitle());
        resume.changeContent(resumeDto.getContent());
        resume.changePrice(resumeDto.getPrice());
        resume.changeStack(resumeDto.getStack());
        resume.setStatus(ResumeStatus.modified);
        saveResumeLog(resume, ResumeStatus.modified);

        Category category = CategoryUtil.getOrCreateCategory(categoryRepository, resumeDto.getCategory());
        resume.changeCategory(category);

        if (resumeFile != null && !resumeFile.isEmpty()) {
            String resumeFilePath = s3Uploader.upload(resumeFile);
            resume.changeResumeFilePath(resumeFilePath);
        }
        resume.clearList();
        uploadResumeImages(images, resume);
    }

    private void saveResumeLog(Resume resume, ResumeStatus status) {
        ResumeLog history = ResumeLog.builder()
                .resumeId(resume.getResumeId())
                .title(resume.getTitle())
                .status(status)
                .build();
        resumeLogRepository.save(history);
    }

}
