package com.devcv.auth.application;

import com.devcv.auth.details.MemberDetails;
import com.devcv.auth.jwt.JwtProvider;
import com.devcv.auth.jwt.JwtTokenDto;
import com.devcv.auth.jwt.RefreshToken;
import com.devcv.common.exception.ErrorCode;
import com.devcv.member.domain.Member;
import com.devcv.member.domain.MemberLog;
import com.devcv.member.domain.dto.MemberLoginRequest;
import com.devcv.member.domain.dto.MemberLoginResponse;
import com.devcv.member.domain.dto.MemberSignUpRequest;
import com.devcv.member.domain.enumtype.RoleType;
import com.devcv.member.domain.enumtype.SocialType;
import com.devcv.member.exception.AuthLoginException;
import com.devcv.member.exception.DuplicationException;
import com.devcv.member.exception.NotNullException;
import com.devcv.member.repository.MemberLogRepository;
import com.devcv.member.repository.MemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final MemberDetailsService memberDetailsService;
    private final MemberRepository memberRepository;
    private final MemberLogRepository memberLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    @Value("${keys.social_password}")
    private String socialPassword;

    @Transactional
    public void signup(MemberSignUpRequest memberSignUpRequest) {
        if (memberRepository.findMemberByEmail(memberSignUpRequest.getEmail()) != null) {
            throw new DuplicationException(ErrorCode.DUPLICATE_ERROR);
        }
        // RequestContextHolder에서 request 객체 구하기
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        Member member = Member.builder()
                .memberName(memberSignUpRequest.getMemberName()).email(memberSignUpRequest.getEmail())
                .password(passwordEncoder.encode(memberSignUpRequest.getSocial().name().equals(SocialType.normal.name()) ? memberSignUpRequest.getPassword() : socialPassword))
                .nickName(memberSignUpRequest.getNickName()).phone(memberSignUpRequest.getPhone()).address(memberSignUpRequest.getAddress()).social(memberSignUpRequest.getSocial())
                .company(memberSignUpRequest.getCompany()).job(memberSignUpRequest.getJob()).stack(memberSignUpRequest.getStack())
                .memberRole(RoleType.normal)
                .build();
        try{
            if(member.getMemberName() == null || member.getNickName() == null || member.getEmail() == null
                || member.getPassword() == null || member.getPhone() == null || member.getAddress() == null
                || member.getSocial() == null){
                throw new NotNullException(ErrorCode.NULL_ERROR);
            } else {
                memberRepository.save(member);
                memberLogRepository.save(MemberLog.builder().logAgent(request.getHeader("user-agent")).logEmail(member.getEmail())
                        .logIp(getIp(request)).logSignUpDate(LocalDateTime.now()).memberId(member.getMemberId()).build());
            }
        } catch (NotNullException e) {
            throw new NotNullException(ErrorCode.NULL_ERROR);
        }
    }
    @Transactional
    public MemberLoginResponse login(MemberLoginRequest memberLoginRequest) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        MemberDetails memberDetails = (MemberDetails) memberDetailsService.loadUserByUsername(memberLoginRequest.getEmail());
        // 1. Login ID/PW 로 AuthenticationToken 생성
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(memberLoginRequest.getEmail(),
                memberLoginRequest.getPassword(),memberDetails.getAuthorities());
        // 2. 검증 (비밀번호 체크)
        // authenticate -> MemberDetailsService (loadUserByUsername) 실행
        try {
            Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
            // 3. 인증 정보를 기반으로 JWT 토큰 생성
            JwtTokenDto tokenDto = jwtProvider.generateTokenDto(authentication);
            // 4. RefreshToken 저장
            RefreshToken refreshToken = RefreshToken.builder()
                    .key(authentication.getName())
                    .value(tokenDto.getRefreshToken())
                    .build();
            memberRepository.updateRefreshTokenBymemberId(memberDetails.getMember().getMemberId(), refreshToken.getValue());
            // memberLoginLog save
            memberLogRepository.save(MemberLog.builder().logLoginDate(LocalDateTime.now())
                    .logEmail(memberLoginRequest.getEmail())
                    .logIp(getIp(request)).logAgent(request.getHeader("user-agent"))
                    .memberId(memberDetails.getMember().getMemberId())
                    .build());
            // 5. 토큰 발급
            return MemberLoginResponse.from(tokenDto, authentication);
        } catch (BadCredentialsException e){
            e.fillInStackTrace();
            throw new AuthLoginException(ErrorCode.LOGIN_ERROR);
        }
    }

    private String getIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

}
