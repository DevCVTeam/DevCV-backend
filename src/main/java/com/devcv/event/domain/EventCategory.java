package com.devcv.event.domain;

import com.devcv.common.exception.BadRequestException;
import com.devcv.common.exception.ErrorCode;
import lombok.Getter;

@Getter
public enum EventCategory {
    ATTENDANCE("출석체크"),
    JOIN("회원가입");

    private final String description;

    EventCategory(String description) {
        this.description = description;
    }

    public static EventCategory strToEnum(String value) {
        try {
            return EventCategory.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(ErrorCode.INVALID_ENUM_EXCEPTION);
        }
    }
}
