package com.devcv.common.handle;

import com.devcv.auth.exception.*;
import com.devcv.common.exception.*;
import com.devcv.common.exception.dto.ErrorResponse;
import com.devcv.common.exception.dto.ValidErrorResponse;
import com.devcv.member.exception.*;
import com.devcv.resume.exception.*;
import com.devcv.member.exception.AuthLoginException;
import com.devcv.member.exception.DuplicationException;
import com.devcv.member.exception.NotNullException;
import com.devcv.member.exception.NotSignUpException;
import com.devcv.order.exception.OrderNotFoundException;
import com.devcv.review.exception.AlreadyExistsException;
import com.devcv.review.exception.ReviewNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.PropertyValueException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;


@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 400
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handle(HttpMessageNotReadableException e) {
        log.error(e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.from(ErrorCode.EMPTY_VALUE_ERROR));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handle(BadRequestException e) {
        log.error(e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.from(e));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handle(NotFoundException e) {
        log.error(e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.from(e));
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ErrorResponse> handle(MultipartException e) {
        log.error(e.getMessage());
        return  ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.from(ErrorCode.FILE_SIZE_LIMIT_EXCEEDED));
    }

    @ExceptionHandler(FileNameLengthExceededException.class)
    public ResponseEntity<ErrorResponse> handle(FileNameLengthExceededException e) {
        log.error(e.getMessage());
        return  ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.from(ErrorCode.FILE_NAME_LENGTH_EXCEEDED));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<Object> handle(MethodArgumentNotValidException e) {
        log.error(e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ValidErrorResponse.from(e));
    }

    // 401
    @ExceptionHandler(UnAuthorizedException.class)
    public ResponseEntity<ErrorResponse> handle(UnAuthorizedException e) {
        log.error(e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.from(ErrorCode.UNAUTHORIZED_ERROR));
    }
    @ExceptionHandler(DuplicationException.class)
    public ResponseEntity<ErrorResponse> handle(DuplicationException e) {
        log.error(e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.from(ErrorCode.DUPLICATE_ERROR));
    }
    @ExceptionHandler(AuthLoginException.class)
    public ResponseEntity<ErrorResponse> handle(AuthLoginException e) {
        log.error(e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.from(ErrorCode.LOGIN_ERROR));
    }
    @ExceptionHandler(PropertyValueException.class)
    public ResponseEntity<ErrorResponse> handle(PropertyValueException e) {
        log.error(e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.from(ErrorCode.NULL_ERROR));
    }
    @ExceptionHandler(SocialDataException.class)
    public ResponseEntity<ErrorResponse> handle(SocialDataException e) {
        log.error(e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.from(ErrorCode.SOCIAL_ERROR));
    }
    @ExceptionHandler(JwtExpiredException.class)
    public ResponseEntity<ErrorResponse> handle(JwtExpiredException e) {
        log.error(e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.from(ErrorCode.JWT_EXPIRED_ERROR));
    }
    @ExceptionHandler(JwtUnsupportedException.class)
    public ResponseEntity<ErrorResponse> handle(JwtUnsupportedException e) {
        log.error(e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.from(ErrorCode.JWT_UNSUPPORTED_ERROR));
    }
    @ExceptionHandler(JwtIllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handle(JwtIllegalArgumentException e) {
        log.error(e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.from(ErrorCode.JWT_ILLEGALARGUMENT_ERROR));
    }
    @ExceptionHandler(JwtInvalidSignException.class)
    public ResponseEntity<ErrorResponse> handle(JwtInvalidSignException e) {
        log.error(e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.from(ErrorCode.JWT_INVALID_SIGN_ERROR));
    }
    @ExceptionHandler(SocialMemberUpdateException.class)
    public ResponseEntity<ErrorResponse> handle(SocialMemberUpdateException e) {
        log.error(e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.from(ErrorCode.SOCIAL_UPDATE_ERROR));
    }
    @ExceptionHandler(SocialLoginException.class)
    public ResponseEntity<ErrorResponse> handle(SocialLoginException e) {
        log.error(e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.from(ErrorCode.SOCIAL_LOGIN_ERROR));
    }
    @ExceptionHandler(JwtNotExpiredException.class)
    public ResponseEntity<ErrorResponse> handle(JwtNotExpiredException e) {
        log.error(e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.from(ErrorCode.JWT_NOT_EXPIRED_ERROR));
    }
    @ExceptionHandler(NotMatchMemberIdException.class)
    public ResponseEntity<ErrorResponse> handle(NotMatchMemberIdException e) {
        log.error(e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.from(ErrorCode.MEMBERID_ERROR));
    }

    // 403
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handle(ForbiddenException e) {
        log.error(e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.from(e));
    }

    // 404
    @ExceptionHandler(NotNullException.class)
    public ResponseEntity<ErrorResponse> handle(NotNullException e) {
        log.error(e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.from(ErrorCode.NULL_ERROR));
    }
    @ExceptionHandler(NotSignUpException.class)
    public ResponseEntity<ErrorResponse> handle(NotSignUpException e) {
        log.error(e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.from(ErrorCode.FIND_ID_ERROR));
    }

    @ExceptionHandler(ResumeNotFoundException.class)
    public ResponseEntity<ErrorResponse> handle(ResumeNotFoundException e) {
        log.error(e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.from(ErrorCode.RESUME_NOT_FOUND));
    }
    @ExceptionHandler(MemberNotFoundException.class)
    public ResponseEntity<ErrorResponse> handle(MemberNotFoundException e) {
        log.error(e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.from(ErrorCode.MEMBER_NOT_FOUND));
    }
    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handle(OrderNotFoundException e) {
        log.error(e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.from(ErrorCode.ORDER_NOT_FOUND));
    }

    @ExceptionHandler(ReviewNotFoundException.class)
    public ResponseEntity<ErrorResponse> handle(ReviewNotFoundException e) {
        log.error(e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.from(ErrorCode.REVIEW_NOT_FOUND));
    }

    @ExceptionHandler(ResumeNotExistException.class)
    public ResponseEntity<ErrorResponse> handle(ResumeNotExistException e) {
        log.error(e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.from(ErrorCode.RESUME_NOT_EXIST));
    }

    @ExceptionHandler(JwtNotFoundRefreshTokenException.class)
    public ResponseEntity<ErrorResponse> handle(JwtNotFoundRefreshTokenException e) {
        log.error(e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.from(ErrorCode.REFRESHTOKEN_NOT_FOUND));
    }

    // 409 start
    @ExceptionHandler(AlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handle(AlreadyExistsException e) {
        log.error(e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.from(e));
    }

    // s3
    @ExceptionHandler(S3Exception.class)
    public ResponseEntity<ErrorResponse>handle(S3Exception e) {
        System.out.println("s3 upload error");
        log.error(e.getMessage());

        ErrorCode errorCode = e.getErrorCode();
        HttpStatus status;

        switch (errorCode) {
            case EMPTY_FILE_EXCEPTION:
            case NO_FILE_EXTENTION:
            case INVALID_FILE_EXTENTION:
                status = HttpStatus.BAD_REQUEST;
                break;
            case IO_EXCEPTION_ON_IMAGE_UPLOAD:
            case PUT_OBJECT_EXCEPTION:
                status = HttpStatus.INTERNAL_SERVER_ERROR;
                break;
            default:
                status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return ResponseEntity.status(status)
                .body(ErrorResponse.from(errorCode));
    }

    // 500 start
    @ExceptionHandler(TestErrorException.class)
    public ResponseEntity<ErrorResponse> handle(TestErrorException e) {
        log.error(e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.from(ErrorCode.TEST_ERROR));
    }

    @ExceptionHandler(ResumeStatusException.class)
    public ResponseEntity<ErrorResponse> handle(ResumeStatusException e) {
        log.error(e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.from(ErrorCode.RESUME_NOT_APPROVAL));
    }

    @ExceptionHandler(InternalServerException.class)
    public ResponseEntity<ErrorResponse> handle(InternalServerException e) {
        log.error(e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.from(ErrorCode.INTERNAL_SERVER_ERROR));
    }
}