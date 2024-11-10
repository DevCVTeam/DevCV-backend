package com.devcv.point.application;

import com.devcv.member.domain.Member;
import com.devcv.point.domain.Point;
import com.devcv.point.domain.PointPolicy;
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
    private final PointPolicy pointPolicy;

    public Long getMyPoint(Long memberId) {
        Long point = pointRepository.findTotalPointsBymemberId(memberId);
        return Objects.requireNonNullElse(point, 0L);
    }

    public PointResponse getPointResponse(Long memberId) {
        return PointResponse.of(memberId, getMyPoint(memberId));
    }

    @Transactional
    public void usePoint(Member member, Long amount, String orderNumber) {
        Point point = Point.use(member, amount, pointPolicy.getOrderDescription(orderNumber));
        pointRepository.save(point);
    }

    @Transactional
    public void awardSignUpBonus(Member member) {
        Point point = Point.save(member, pointPolicy.getSignUpBonusAmount(), pointPolicy.getSignupDescription());
        pointRepository.save(point);
    }

    @Transactional
    public void rewardSalesPoint(Member member, Long saleAmount, String orderNumber) {
        Point point = Point.save(member, pointPolicy.calculateSalesReward(saleAmount),
                pointPolicy.getSalesRewardDescription() + orderNumber);
        pointRepository.save(point);
    }

    @Transactional
    public void awardAttendancePoint(Member member, Long eventAmount, String description) {
        Point point = Point.save(member, eventAmount, description);
        pointRepository.save(point);
    }
}
