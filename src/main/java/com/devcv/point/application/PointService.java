package com.devcv.point.application;

import com.devcv.member.domain.Member;
import com.devcv.point.domain.Point;
import com.devcv.point.dto.PointResponse;
import com.devcv.point.repository.PointRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PointService {

    private final PointRepository pointRepository;

    public Long getMyPoint(Long memberId) {
        Long point = pointRepository.findTotalPointsBymemberId(memberId);
        return Objects.requireNonNullElse(point, 0L);
    }

    public PointResponse getPointResponse(Long memberId) {
        return PointResponse.of(memberId, getMyPoint(memberId));
    }

    @Transactional
    public Point savePoint(Member member, Long amount, String description) {

        Point point = Point.save(member, amount, description);

        return pointRepository.save(point);
    }

    @Transactional
    public Point usePoint(Member member, Long amount, String description) {

        Point point = Point.use(member, amount, description);

        return pointRepository.save(point);
    }
}
