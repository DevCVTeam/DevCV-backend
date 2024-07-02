package com.devcv.order.domain.dto;

import com.devcv.member.domain.dto.MemberResponse;

public record OrderSheet(MemberResponse memberResponse, OrderSheetResume resumeResponse) {

    public static OrderSheet of(MemberResponse memberResponse, OrderSheetResume orderSheetResume) {
        return new OrderSheet(memberResponse, orderSheetResume);
    }
}