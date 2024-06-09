package com.devcv.register.application;

import com.devcv.member.domain.Member;
import com.devcv.member.domain.dto.MemberResponse;
import com.devcv.member.repository.MemberRepository;
import com.devcv.register.domain.Resume;
import com.devcv.register.domain.Category;
import com.devcv.common.util.S3Uploader;
import com.devcv.register.domain.ResumeImage;
import com.devcv.register.domain.dto.CategoryDTO;
import com.devcv.register.domain.dto.ResumeRequest;
import com.devcv.register.domain.enumtype.ResumeStatus;
import com.devcv.register.repository.CategoryRepository;
import com.devcv.register.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;



@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ResumeServiceImpl implements ResumeService {

   private  final ResumeRepository resumeRepository;
   private final CategoryRepository categoryRepository;
    private final MemberRepository memberRepository;
   private final S3Uploader s3Uploader;

    @Override
    public Resume register(MemberResponse memberResponse, ResumeRequest resumeRequest) {
        try {

            Member member = memberRepository.findMemberByUserId(memberResponse.getUserId());

            // Category 저장
            CategoryDTO categoryDTO = resumeRequest.getCategory();
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
            if (resumeRequest.getResumeFile() != null) {
                resumeFilePath = s3Uploader.upload(resumeRequest.getResumeFile());
                log.debug("Uploaded resume file path: {}", resumeFilePath);
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
            List<MultipartFile> imageFiles = resumeRequest.getImageFiles();
            if (imageFiles != null && !imageFiles.isEmpty()) {
                for (int i = 0; i < imageFiles.size(); i++) {
                    String imagePath = s3Uploader.upload(imageFiles.get(i));
                    ResumeImage resumeImage = ResumeImage.builder()
                            .resumeImgPath(imagePath)
                            .ord(i)
                            .build();
                    resume.addImage(resumeImage);
                }
            }


            // 상태 설정
            resume.setStatus(ResumeStatus.승인대기);

            return resumeRepository.save(resume);


        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file", e);
        }
    }

}
