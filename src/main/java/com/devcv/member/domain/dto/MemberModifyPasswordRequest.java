package com.devcv.member.domain.dto;

import com.devcv.common.exception.ErrorCode;
import com.devcv.member.exception.NotNullException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Builder(toBuilder = true)
public class MemberModifyPasswordRequest {
    private String password;

    public MemberModifyPasswordRequest(String password) {
        this.password = password;
        // NULL CHECK
        try {
            if(this.password == null){
                throw new NotNullException(ErrorCode.NULL_ERROR);
            }
        } catch (Exception e) {
            e.fillInStackTrace();
            throw new NotNullException(ErrorCode.NULL_ERROR);
        }
    }

}
