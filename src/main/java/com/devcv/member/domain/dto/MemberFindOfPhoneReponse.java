package com.devcv.member.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
public class MemberFindOfPhoneReponse {
    private List<Map<String,Object>> findMemberList;
    public static MemberFindOfPhoneReponse from(List<Map<String,Object>> memberList){
        return new MemberFindOfPhoneReponse(memberList);
    }
}
