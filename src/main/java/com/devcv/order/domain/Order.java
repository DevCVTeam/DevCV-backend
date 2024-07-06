package com.devcv.order.domain;

import com.devcv.common.domain.BaseTimeEntity;
import com.devcv.member.domain.Member;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Table(name = "tb_order")
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    @Column(unique = true, nullable = false)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column
    private Long totalPrice;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderResume> orderResumeList;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private OrderStatus orderStatus;

    @Column
    private PayType payType;

    public Order(Long orderId, String orderNumber, Member member, Long totalPrice, List<OrderResume> orderResumeList,
                 OrderStatus orderStatus, PayType payType) {
        this.orderId = orderId;
        this.orderNumber = orderNumber;
        this.member = member;
        this.totalPrice = totalPrice;
        this.orderResumeList = orderResumeList;
        this.orderStatus = orderStatus;
        this.payType = payType;
    }

    public void updateOrderResumeList(List<OrderResume> orderResumeList) {
        this.orderResumeList = orderResumeList;
        this.totalPrice = calculateTotalPrice(orderResumeList);
        this.orderStatus = OrderStatus.COMPLETED;
    }

    public static Order init(Member member) {
        return new Order(null, OrderNumberGenerator.generateOrderNumber(), member, 0L,
                null, OrderStatus.PENDING, PayType.POINT);
    }

    private Long calculateTotalPrice(List<OrderResume> resumeList) {
        return resumeList.stream().mapToLong(OrderResume::getPrice).sum();
    }
}