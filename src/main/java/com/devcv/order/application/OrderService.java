package com.devcv.order.application;

import com.devcv.common.exception.ErrorCode;
import com.devcv.common.exception.BadRequestException;
import com.devcv.common.exception.NotFoundException;
import com.devcv.member.domain.Member;
import com.devcv.member.domain.dto.MemberResponse;
import com.devcv.order.domain.Order;
import com.devcv.order.domain.OrderResume;
import com.devcv.order.domain.dto.*;
import com.devcv.order.exception.OrderNotFoundException;
import com.devcv.order.repository.OrderResumeRepository;
import com.devcv.point.application.PointService;
import com.devcv.resume.domain.Resume;
import com.devcv.order.repository.OrderRepository;
import com.devcv.resume.domain.enumtype.ResumeStatus;
import com.devcv.resume.repository.ResumeRepository;
import com.devcv.review.exception.AlreadyExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final static String DESCRIPTION = "상품 구매 - 주문번호:";

    private final OrderRepository orderRepository;
    private final ResumeRepository resumeRepository;
    private final OrderResumeRepository orderResumeRepository;
    private final PointService pointService;

    public OrderSheet getOrderSheet(Member member, Long resumeId) {
        Resume resume = resumeRepository.findByResumeId(resumeId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.RESUME_NOT_FOUND));
        OrderSheetResume orderSheetResume = OrderSheetResume.from(resume);

        Long currentPoint = pointService.getMyPoint(member.getMemberId());
        MemberResponse memberResponse = MemberResponse.of(member, currentPoint);
        return OrderSheet.of(memberResponse, orderSheetResume);
    }

    @Transactional
    public Order createOrder(Member member, CartOrderRequest cartOrderRequest) {
        validateTotalPrice(cartOrderRequest);
        validateMyPoint(member, cartOrderRequest);
        List<Long> existingResumeIdList = orderRepository.getResumeIdsByMemberId(member.getMemberId());
        Order order = orderRepository.save(Order.init(member));

        List<OrderResume> orderResumeList = new ArrayList<>();
        for (CartDto dto : cartOrderRequest.cartList()) {
            Resume resume = resumeRepository.findByResumeId(dto.resumeId())
                    .orElseThrow(() -> new NotFoundException(ErrorCode.RESUME_NOT_FOUND));
            validateResume(resume, dto, existingResumeIdList);
            OrderResume orderResume = OrderResume.of(order, resume);
            orderResumeList.add(orderResume);
        }
        validateTotalPriceForDB(cartOrderRequest, orderResumeList);
        orderResumeRepository.saveAll(orderResumeList);
        pointService.usePoint(member, cartOrderRequest.totalPrice(), DESCRIPTION + order.getOrderNumber());
        order.updateOrderResumeList(orderResumeList);
        return order;
    }

    private void validateTotalPrice(CartOrderRequest cartOrderRequest) {
        Long totalPrice = cartOrderRequest.totalPrice();
        Long dtoPrice = cartOrderRequest.cartList().stream().mapToLong(CartDto::price).sum();
        if (!totalPrice.equals(dtoPrice)) {
            throw new BadRequestException(ErrorCode.ORDER_INFO_MISMATCH_EXCEPTION);
        }
    }

    private void validateMyPoint(Member member, CartOrderRequest cartOrderRequest) {
        Long totalPrice = cartOrderRequest.totalPrice();
        Long myPoint = pointService.getMyPoint(member.getMemberId());
        if (myPoint < totalPrice) {
            throw new BadRequestException(ErrorCode.INSUFFICIENT_POINT);
        }
    }

    private void validateResume(Resume resume, CartDto dto, List<Long> existingResumeIdList) {
        checkResumeStatus(resume);
        validateCartDtoWithOrigin(resume, dto);
        checkDuplicate(resume, existingResumeIdList);
    }

    private void checkResumeStatus(Resume resume) {
        if (!resume.getStatus().equals(ResumeStatus.regcompleted)) {
            throw new BadRequestException(ErrorCode.RESUME_STATUS_EXCEPTION);
        }
    }

    private void validateCartDtoWithOrigin(Resume origin, CartDto dto) {
        if (!origin.getResumeId().equals(dto.resumeId())) {
            throw new BadRequestException(ErrorCode.ORDER_INFO_MISMATCH_EXCEPTION);
        }
        if (origin.getPrice() != dto.price()) {
            throw new BadRequestException(ErrorCode.ORDER_INFO_MISMATCH_EXCEPTION);
        }
    }

    private void checkDuplicate(Resume resume, List<Long> existingIds) {
        if (existingIds.stream().anyMatch(existingId -> existingId.equals(resume.getResumeId()))){
            throw new AlreadyExistsException(ErrorCode.ALREADY_EXISTS_ORDER);
        }
    }

    private void validateTotalPriceForDB(CartOrderRequest cartOrderRequest, List<OrderResume> orderResumeList) {
        Long inputTotalPrice = cartOrderRequest.totalPrice();
        Long dbTotalPrice = orderResumeList.stream().mapToLong(OrderResume::getPrice).sum();
        if (!inputTotalPrice.equals(dbTotalPrice)) {
            throw new BadRequestException(ErrorCode.ORDER_INFO_MISMATCH_EXCEPTION);
        }
    }

    public OrderResponse findByOrderNumber(String orderNumber, Member member) {
        Order order = orderRepository.findOrderByOrderNumberAndMember(orderNumber, member)
                .orElseThrow(() -> new OrderNotFoundException(ErrorCode.ORDER_NOT_FOUND));
        return getOrderResponse(order);
    }

    private OrderResponse getOrderResponse(Order order) {
        List<OrderResume> orderResumeList = orderResumeRepository.findAllByOrder_OrderId(order.getOrderId());
        return OrderResponse.of(order, orderResumeListToDto(orderResumeList));
    }

    private List<OrderResumeDto> orderResumeListToDto(List<OrderResume> orderResumeList) {
        return orderResumeList.stream().map(OrderResume::getResume)
                .map(OrderResumeDto::from).collect(Collectors.toList());
    }

    public OrderListResponse getOrderListByMember(Member member) {
        List<OrderResponse> orderList = orderRepository.findOrderListByMember(member).stream()
                .map(this::getOrderResponse)
                .collect(Collectors.toList());
        int count = orderList.size();
        return OrderListResponse.of(member.getMemberId(), count, orderList);
    }
}