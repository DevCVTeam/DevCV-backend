package com.devcv.resume.exception;

import com.devcv.common.exception.CustomException;
import com.devcv.common.exception.ErrorCode;

public class ResumeNotFoundException extends CustomException {
    public ResumeNotFoundException(ErrorCode errorCode) {super(errorCode);}

    public ResumeNotFoundException(ErrorCode errorCode, String message) {super(errorCode, message);}
}