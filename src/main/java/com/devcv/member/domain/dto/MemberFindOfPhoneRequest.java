package com.devcv.member.domain.dto;

import com.devcv.common.exception.ErrorCode;
import com.devcv.member.exception.NotNullException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public class MemberFindOfPhoneRequest {
    private String memberName;
    private String phone;
    public MemberFindOfPhoneRequest (String memberName, String phone) {
        this.memberName = memberName;
        this.phone = phone;
        // NULL CHECK
        try{
            if(this.memberName == null || this.phone == null){
                throw new NotNullException(ErrorCode.NULL_ERROR);
            }
        } catch (NotNullException e){
            throw new NotNullException(ErrorCode.NULL_ERROR);
        }
    }
}
