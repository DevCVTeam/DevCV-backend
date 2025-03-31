package com.devcv.resume.infrastructure;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.util.IOUtils;
import com.devcv.common.exception.ErrorCode;
import com.devcv.resume.exception.FileNameLengthExceededException;
import com.devcv.resume.exception.S3Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3Uploader {

    private final AmazonS3 amazonS3;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;

    // 최대 파일 제목 길이, 50자
    private static final int MAX_FILE_NAME_LENGTH = 50;

    //외부 호출 가능 메서드
    public String upload(MultipartFile image) {
        //입력받은 이미지 파일이 빈 파일인지 검증
        if(image.isEmpty() || Objects.isNull(image.getOriginalFilename())){
            throw new S3Exception(ErrorCode.EMPTY_FILE_EXCEPTION);
        }
        if (image.getOriginalFilename().length() > MAX_FILE_NAME_LENGTH) {
            throw new FileNameLengthExceededException(ErrorCode.FILE_NAME_LENGTH_EXCEEDED);
        }
        //uploadImage 호출
        return this.uploadImage(image);
    }


    private String uploadImage(MultipartFile image) {
        // validateImageFileExtention() 호출
        this.validateImageFileExtension(image.getOriginalFilename());
        try {
            //uploadImageToS3() 호출
            return this.uploadImageToS3(image);
        } catch (IOException e) {
            throw new S3Exception(ErrorCode.IO_EXCEPTION_ON_IMAGE_UPLOAD);
        }
    }
    // 확장자 검사
    private void validateImageFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1) {
            throw new S3Exception(ErrorCode.NO_FILE_EXTENTION);
        }
        // filename을 받아서 파일 확장자가 jpg, jpeg, png, gif, pdf, hwp, docx 중에 속하는지 검증한다.
        String extension = filename.substring(lastDotIndex + 1).toLowerCase();
        List<String> allowedExtensionList = Arrays.asList("jpg", "jpeg", "png", "gif", "pdf", "hwp", "docx", "heic", "webp");

        if (!allowedExtensionList.contains(extension)) {
            throw new S3Exception(ErrorCode.INVALID_FILE_EXTENTION);
        }
    }

    // S3에 이미지 업로드 메서드, public url 반환
    private String uploadImageToS3(MultipartFile image) throws IOException {
        String originalFilename = image.getOriginalFilename(); // 원본 파일 명
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".")); // 확장자 명

        String s3FileName = UUID.randomUUID().toString().substring(0, 10) + originalFilename; // 변경된 파일 명

        InputStream is = image.getInputStream();
        byte[] bytes = IOUtils.toByteArray(is);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(image.getContentType());
        metadata.setContentLength(bytes.length);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);

        try {
            PutObjectRequest putObjectRequest =
                    new PutObjectRequest(bucketName, s3FileName, byteArrayInputStream, metadata);
            amazonS3.putObject(putObjectRequest); // S3 putObject 정책 실행
        } catch (Exception e) {
            log.error("Error putting object to S3", e);
            throw new S3Exception(ErrorCode.PUT_OBJECT_EXCEPTION);
        } finally {
            byteArrayInputStream.close();
            is.close();
        }

        // 최종 url 반환
        return  amazonS3.getUrl(bucketName, s3FileName).toString();
    }

}