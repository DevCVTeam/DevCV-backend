package com.devcv.auth.exception;

import com.devcv.common.exception.CustomException;
import com.devcv.common.exception.ErrorCode;

public class JwtNotFoundRefreshTokenException extends CustomException {
    public JwtNotFoundRefreshTokenException(ErrorCode errorCode) { super(errorCode); }

    public JwtNotFoundRefreshTokenException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
