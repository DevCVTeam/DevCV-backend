package com.devcv.member.domain;

import com.devcv.common.domain.BaseTimeEntity;
import com.devcv.member.domain.enumtype.CompanyType;
import com.devcv.member.domain.enumtype.JobType;
import com.devcv.member.domain.enumtype.RoleType;
import com.devcv.member.domain.enumtype.SocialType;
import com.devcv.member.infrastructure.ListStringConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Table(name = "tb_member")
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "memberId")
    private Long memberId;

    @Column(name = "memberName", nullable = false)
    private String memberName;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "nickname", nullable = false)
    private String nickName;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "address", nullable = false)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "social", nullable = false)
    private SocialType social; // 일반유저, 구글, 카카오

    @Enumerated(EnumType.STRING)
    @Column(name = "memberRole", nullable = false)
    private RoleType memberRole; // 일반유저: user 관리자: admin

    @Enumerated(EnumType.STRING)
    @Column(name = "company")
    private CompanyType company;

    @Enumerated(EnumType.STRING)
    @Column(name = "job")
    private JobType job;

    @Convert(converter = ListStringConverter.class)
    @Column(name = "stack")
    private List<String> stack;

    @Column(name = "refreshToken")
    private String refreshToken;
}


