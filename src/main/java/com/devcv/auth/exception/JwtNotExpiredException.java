package com.devcv.auth.exception;

import com.devcv.common.exception.CustomException;
import com.devcv.common.exception.ErrorCode;

public class JwtNotExpiredException extends CustomException {
    public JwtNotExpiredException(ErrorCode errorCode) { super(errorCode); }

    public JwtNotExpiredException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
