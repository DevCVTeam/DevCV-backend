package com.devcv.order.domain;

import com.devcv.resume.domain.Resume;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Table(name = "tb_order_resume")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderResume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderResumeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @Column(nullable = false)
    private Long price;

    public OrderResume(Long orderResumeId, Order order, Resume resume, Long price) {
        this.orderResumeId = orderResumeId;
        this.order = order;
        this.resume = resume;
        this.price = price;
    }

    public static OrderResume of(Order order, Resume resume) {
        return new OrderResume(null, order, resume, (long) resume.getPrice());
    }
}
