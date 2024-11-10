package com.devcv.point.presentation;

import com.devcv.auth.filter.SecurityUtil;
import com.devcv.common.exception.ErrorCode;
import com.devcv.common.exception.ForbiddenException;
import com.devcv.point.application.PointService;
import com.devcv.point.dto.PointResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    @GetMapping("/members/{member-id}/points")
    public ResponseEntity<PointResponse> getPointByMemberId(@PathVariable("member-id") Long memberId) {
        if (!memberId.equals(SecurityUtil.getCurrentmemberId())) {
            throw new ForbiddenException(ErrorCode.MEMBER_MISMATCH_EXCEPTION);
        }
        PointResponse pointResponse = pointService.getPointResponse(memberId);
        return ResponseEntity.ok().body(pointResponse);
    }
}
