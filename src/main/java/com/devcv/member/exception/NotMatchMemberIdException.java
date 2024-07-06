package com.devcv.member.exception;

import com.devcv.common.exception.CustomException;
import com.devcv.common.exception.ErrorCode;

public class NotMatchMemberIdException extends CustomException {
    public NotMatchMemberIdException(ErrorCode errorCode) {super(errorCode);}

    public NotMatchMemberIdException(ErrorCode errorCode, String message) {super(errorCode, message);}
}
