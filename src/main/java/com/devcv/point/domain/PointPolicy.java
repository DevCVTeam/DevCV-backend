package com.devcv.point.domain;

import org.springframework.stereotype.Component;

@Component
public class PointPolicy {
    private final static Long SIGNUP_BONUS = 5000L;
    private final static String SIGNUP_DESCRIPTION = "회원 가입 보상";

    private final static double SALES_REWARD_RATE = 0.1;
    private final static String SALES_REWARD_DESCRIPTION = "이력서 판매 적립 - 주문 번호:";

    private final static String ORDER_DESCRIPTION = "이력서 구매 - 주문 번호:";


    public Long getSignUpBonusAmount() {
        return SIGNUP_BONUS;
    }

    public String getSignupDescription() {
        return SIGNUP_DESCRIPTION;
    }

    public Long calculateSalesReward(Long saleAmount) {
        return (long) (saleAmount * SALES_REWARD_RATE);
    }

    public String getSalesRewardDescription() {
        return SALES_REWARD_DESCRIPTION;
    }

    public String getOrderDescription(String orderNumber) {
        return ORDER_DESCRIPTION + orderNumber;
    }
}
