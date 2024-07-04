package com.devcv.member.presentation;

import com.devcv.auth.application.AuthService;
import com.devcv.auth.details.MemberDetails;
import com.devcv.auth.exception.JwtNotExpiredException;
import com.devcv.auth.exception.JwtNotFoundRefreshTokenException;
import com.devcv.auth.jwt.JwtProvider;
import com.devcv.auth.jwt.JwtTokenDto;
import com.devcv.common.exception.ErrorCode;
import com.devcv.common.exception.InternalServerException;
import com.devcv.common.exception.UnAuthorizedException;
import com.devcv.member.application.MailService;
import com.devcv.member.application.MemberService;
import com.devcv.member.domain.Member;
import com.devcv.member.domain.MemberLog;
import com.devcv.member.domain.dto.*;
import com.devcv.member.domain.dto.profile.GoogleProfile;
import com.devcv.member.domain.dto.profile.KakaoProfile;
import com.devcv.member.domain.enumtype.SocialType;
import com.devcv.member.exception.*;
import com.devcv.member.repository.MemberLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/members/*")
@PropertySource("classpath:application.yml")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final MailService mailService;
    private final MemberLogRepository memberLogRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
    @Value("${keys.social_password}")
    private String socialPassword;

//-------------------------------------------------------- 이메일인증 start --------------------------------------------------------
    @GetMapping("/cert-email")
    public ResponseEntity<CertificationMailResponse> certEmail(@RequestParam String email) {
        try{
            Long certNumber = mailService.sendMail(email);
            if(certNumber == 0){ // 메일 인증 실패
                throw new InternalServerException(ErrorCode.INTERNAL_SERVER_ERROR);
            }
            return ResponseEntity.ok().body(new CertificationMailResponse(certNumber));
        } catch (UnsupportedEncodingException ue){
            throw new InternalServerException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
//-------------------------------------------------------- 이메일인증 end --------------------------------------------------------
//-------------------------------------------------------- 이메일중복확인 start --------------------------------------------------------
    @GetMapping("/duplication-email")
    public ResponseEntity<Object> duplicationEmail(@RequestParam String email) {
        try {
            Member findMember = memberService.findMemberByEmail(email);
            if(findMember!= null){
                throw new DuplicationException(ErrorCode.DUPLICATE_ERROR);
            } else {
                return ResponseEntity.ok().build();
            }
        } catch (DuplicationException de){
            de.fillInStackTrace();
            throw new DuplicationException(ErrorCode.DUPLICATE_ERROR);
        }
    }
//-------------------------------------------------------- 이메일중복확인 end --------------------------------------------------------

//-------------------------------------------------------- 아이디찾기 start --------------------------------------------------------
    @PostMapping("/find-id")
    public ResponseEntity<MemberFindOfPhoneReponse> findId(@RequestBody MemberFindOfPhoneRequest memberFindOfPhoneRequest) {
        // 아이디 찾기
        try{
            List<Member> findIdMemberList = memberService.findMemberBymemberNameAndPhone(memberFindOfPhoneRequest.getMemberName(), memberFindOfPhoneRequest.getPhone());
            // 이름&핸드폰번호로 가입되어 있는 멤버가 있는지 확인.
            if(!findIdMemberList.isEmpty()){
                List<Map<String,Object>> responseList = new ArrayList<>();
                for (Member findMember: findIdMemberList){
                    responseList.add(new HashMap<>(){{
                        put("email",findMember.getEmail());
                        put("social",findMember.getSocial());
                    }});
                }
                return ResponseEntity.ok().body(MemberFindOfPhoneReponse.from(responseList));
            } else { // 가입되어있지 않다면 Exception 발생.
                throw new NotSignUpException(ErrorCode.FIND_ID_ERROR);
            }
        } catch (NotSignUpException e) {
            e.fillInStackTrace();
            throw new NotSignUpException(ErrorCode.FIND_ID_ERROR);
        }
    }
//-------------------------------------------------------- 아이디찾기 end --------------------------------------------------------
//-------------------------------------------------------- 비밀번호찾기-이메일 start --------------------------------------------------------
    @PostMapping("/find-pw/email")
    public ResponseEntity<MemberFindPwResponse> findPwEmail(@RequestParam String email) {
        // NULL CHECK
        try{
            if(email == null){
                throw new NotNullException(ErrorCode.NULL_ERROR);
            }
        } catch (Exception e){
            throw new NotNullException(ErrorCode.NULL_ERROR);
        }
        // 이메일로 가입되어있는 아이디 찾기
        try {
            Member findpwEmailMember = memberService.findMemberByEmail(email);
            if(findpwEmailMember != null){
                return ResponseEntity.ok().body(new MemberFindPwResponse(findpwEmailMember.getMemberId()));
            } else {
                throw new NotSignUpException(ErrorCode.FIND_ID_ERROR);
            }
        } catch (Exception e){
            e.fillInStackTrace();
            throw new NotSignUpException(ErrorCode.FIND_ID_ERROR);
        }
    }
//-------------------------------------------------------- 비밀번호찾기-이메일 end --------------------------------------------------------
//-------------------------------------------------------- 비밀번호찾기-본인인증 start --------------------------------------------------------
    @PostMapping("/find-pw")
    public ResponseEntity<MemberFindOfPhoneReponse> findPwPhone(@RequestBody MemberFindOfPhoneRequest memberFindOfPhoneRequest) {
        // NULL CHECK
        try{
            if(memberFindOfPhoneRequest.getMemberName() == null || memberFindOfPhoneRequest.getPhone() == null){
                throw new NotNullException(ErrorCode.NULL_ERROR);
            }
        } catch (NotNullException e){
            throw new NotNullException(ErrorCode.NULL_ERROR);
        }

        // 아이디 찾기
        try{
            List<Member> findIdMemberList = memberService.findMemberBymemberNameAndPhone(memberFindOfPhoneRequest.getMemberName(),memberFindOfPhoneRequest.getPhone());
            // 이름&핸드폰번호로 가입되어 있는 멤버가 있는지 확인.
            if(!findIdMemberList.isEmpty()){
                List<Map<String,Object>> responseList = new ArrayList<>();
                for(Member findMember : findIdMemberList){
                    responseList.add(new HashMap<>(){{
                        put("memberId", findMember.getMemberId());
                    }});
                }
                return ResponseEntity.ok().body(new MemberFindOfPhoneReponse(responseList));
            } else { // 가입되어있지 않다면 Exception 발생.
                throw new NotSignUpException(ErrorCode.FIND_ID_ERROR);
            }
        } catch (NotSignUpException e) {
            e.fillInStackTrace();
            throw new NotSignUpException(ErrorCode.FIND_ID_ERROR);
        }
    }
//-------------------------------------------------------- 비밀번호찾기-본인인증 end --------------------------------------------------------

//-------------------------------------------------------- 비밀번호변경 start --------------------------------------------------------
    @PatchMapping("/{member-id}/password")
    public ResponseEntity<String> modiPassword(@PathVariable("member-id") Long memberId, @RequestBody MemberModifyPasswordRequest memberModifyPasswordRequest){
        // NULL CHECK
        try {
            if(memberModifyPasswordRequest.getPassword() == null || memberId == null){
                throw new NotNullException(ErrorCode.NULL_ERROR);
            }
        } catch (Exception e) {
            e.fillInStackTrace();
            throw new NotNullException(ErrorCode.NULL_ERROR);
        }
        // memberId로 찾은 멤버 패스워드 수정.
        try {
            MemberDetails memberDetails = (MemberDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if(!Objects.equals(memberDetails.getMember().getMemberId(), memberId)){
                throw new NotMatchMemberIdException(ErrorCode.MEMBERID_ERROR);
            }
            Member findMemberBymemberId = memberService.findMemberBymemberId(memberId);
            if(findMemberBymemberId != null){
                if(!findMemberBymemberId.getSocial().name().equals(SocialType.normal.name())){
                    throw new SocialMemberUpdateException(ErrorCode.SOCIAL_UPDATE_ERROR);
                }
                HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
                // JWT 복호화
                MemberModifyPasswordRequest memberModifyPasswordRequestAuth = memberModifyPasswordRequest.toBuilder()
                        .password(String.valueOf(jwtProvider.parseClaims(memberModifyPasswordRequest.getPassword()).get("password"))).build();
                int resultUpdatePassword = memberService.updatePasswordBymemberId(passwordEncoder.encode(memberModifyPasswordRequestAuth.getPassword())
                        ,memberId);
                memberLogRepository.save(MemberLog.builder().memberId(findMemberBymemberId.getMemberId()).logIp(getIp(request))
                        .logEmail(findMemberBymemberId.getEmail()).logAgent(request.getHeader("user-agent")).logUpdateDate(LocalDateTime.now()).build());
                if( resultUpdatePassword == 1 ){ // 비밀번호 수정성공.
                  return ResponseEntity.ok().build();
                } else {
                    throw new InternalServerException(ErrorCode.INTERNAL_SERVER_ERROR);
                }
            } else {
                throw new NotSignUpException(ErrorCode.FIND_ID_ERROR);
            }
        } catch (NotSignUpException e) {
            e.fillInStackTrace();
            throw new NotSignUpException(ErrorCode.FIND_ID_ERROR);
        } catch (SocialMemberUpdateException e){
            e.fillInStackTrace();
            throw new SocialMemberUpdateException(ErrorCode.SOCIAL_UPDATE_ERROR);
        } catch (NotMatchMemberIdException e){
            e.fillInStackTrace();
            throw new NotMatchMemberIdException(ErrorCode.MEMBERID_ERROR);
        }
    }
//-------------------------------------------------------- 비밀번호변경 end --------------------------------------------------------
//-------------------------------------------------------- 회원정보조회 start --------------------------------------------------------
    @GetMapping("{member-id}")
    public ResponseEntity<MemberMypageResponse> getMember(@PathVariable("member-id") Long memberId) {
        try {
            // 로그인한 사용자 memberId 확인
            MemberDetails memberDetails = (MemberDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if(!Objects.equals(memberDetails.getMember().getMemberId(), memberId)){
                throw new NotMatchMemberIdException(ErrorCode.MEMBERID_ERROR);
            }
            return ResponseEntity.ok().body(MemberMypageResponse.from(memberService.findMemberBymemberId(memberId)));
        } catch (NotSignUpException e){
            e.fillInStackTrace();
            throw new NotSignUpException(ErrorCode.MEMBER_NOT_FOUND);
        } catch (NotMatchMemberIdException e){
            e.fillInStackTrace();
            throw new NotMatchMemberIdException(ErrorCode.MEMBERID_ERROR);
        }
    }
//-------------------------------------------------------- 회원정보조회 end --------------------------------------------------------
//-------------------------------------------------------- 회원정보수정 start --------------------------------------------------------
    @PutMapping("/{member-id}")
    public ResponseEntity<String> modifyMember(@RequestBody MemberModifyAllRequest memberModifyAllRequest, @PathVariable("member-id") Long memberId) {
        // NULL CHECK
        try {
            if(memberModifyAllRequest.getJob() == null || memberModifyAllRequest.getAddress() == null || memberModifyAllRequest.getStack() == null
                    || memberModifyAllRequest.getEmail() == null || memberModifyAllRequest.getMemberName() == null || memberModifyAllRequest.getSocial() == null
                    || memberModifyAllRequest.getCompany() == null || memberModifyAllRequest.getPhone() == null || memberId == null
                    || memberModifyAllRequest.getNickName() == null || memberModifyAllRequest.getPassword() == null){
                throw new NotNullException(ErrorCode.NULL_ERROR);
            }
        } catch (NotNullException e){
            e.fillInStackTrace();
            throw new NotNullException(ErrorCode.NULL_ERROR);
        }
        try {
            // 로그인한 사용자 memberId 확인
            MemberDetails memberDetails = (MemberDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if(!Objects.equals(memberDetails.getMember().getMemberId(), memberId)){
                throw new NotMatchMemberIdException(ErrorCode.MEMBERID_ERROR);
            }
            // memberId로 Member 찾기
            Member findMemberBymemberId = memberService.findMemberBymemberId(memberId);
            if(findMemberBymemberId != null){
                if(!findMemberBymemberId.getSocial().name().equals(memberModifyAllRequest.getSocial().name())){
                    throw new SocialDataException(ErrorCode.SOCIAL_ERROR);
                }
                // 수정할 이메일이 이미 존재할 떄 (가입한 아이디 제외)
                Member findMemberBymemberEmail = memberService.findMemberByEmail(memberModifyAllRequest.getEmail());
                if(findMemberBymemberEmail != null && !findMemberBymemberId.getEmail().equals(findMemberBymemberEmail.getEmail())){
                    throw new DuplicationException(ErrorCode.DUPLICATE_ERROR);
                }
                if( memberModifyAllRequest.getSocial().name().equals(SocialType.normal.name())) {
                    HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
                    // JWT 복호화
                    MemberModifyAllRequest memberModifyAllRequestAuth = memberModifyAllRequest.toBuilder()
                            .password(String.valueOf(jwtProvider.parseClaims(memberModifyAllRequest.getPassword()).get("password"))).build();
                    // 일반 회원 수정
                    // 수정 가능 : 이름, 비밀번호, 닉네임, 핸드폰, 주소, 기업, 직업, 스택
                    int resultUpdateMember = memberService.updateMemberBymemberId(memberModifyAllRequestAuth.getMemberName(), memberModifyAllRequestAuth.getEmail(),
                            passwordEncoder.encode(memberModifyAllRequestAuth.getSocial().name().equals(SocialType.normal.name()) ? memberModifyAllRequestAuth.getPassword() : socialPassword),
                            memberModifyAllRequestAuth.getNickName(), memberModifyAllRequestAuth.getPhone(), memberModifyAllRequestAuth.getAddress(),
                            memberModifyAllRequestAuth.getCompany().name(), memberModifyAllRequestAuth.getJob().name(),
                            String.join(",", memberModifyAllRequestAuth.getStack()), memberId);
                    memberLogRepository.save(MemberLog.builder().memberId(findMemberBymemberId.getMemberId()).logIp(getIp(request))
                            .logEmail(findMemberBymemberId.getEmail()).logAgent(request.getHeader("user-agent")).logUpdateDate(LocalDateTime.now()).build());
                    if (resultUpdateMember == 1) { // 일반 멤버 수정성공.
                        return ResponseEntity.ok().build();
                    } else {
                        throw new InternalServerException(ErrorCode.INTERNAL_SERVER_ERROR);
                    }
                } else {
                    HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
                    // 소셜 회원 수정
                    // 수정 가능 : 이름, 닉네임, 핸드폰, 주소, 기업, 직업, 스택
                    // 수정 불가 : 이메일, 비밀번호
                    int resultUpdateSocialMember = memberService.updateSocialMemberBymemberId(memberModifyAllRequest.getMemberName(),
                            memberModifyAllRequest.getNickName(), memberModifyAllRequest.getPhone(), memberModifyAllRequest.getAddress(),
                            memberModifyAllRequest.getCompany().name(), memberModifyAllRequest.getJob().name(),
                            String.join(",", memberModifyAllRequest.getStack()), memberId);
                    memberLogRepository.save(MemberLog.builder().memberId(findMemberBymemberId.getMemberId()).logIp(getIp(request))
                            .logEmail(findMemberBymemberId.getEmail()).logAgent(request.getHeader("user-agent")).logUpdateDate(LocalDateTime.now()).build());
                    if (resultUpdateSocialMember == 1) { // 소셜 멤버 수정성공.
                        return ResponseEntity.ok().build();
                    } else {
                        throw new InternalServerException(ErrorCode.INTERNAL_SERVER_ERROR);
                    }
                }
            } else {
                throw new NotSignUpException(ErrorCode.FIND_ID_ERROR);
            }
        } catch (NotSignUpException e) {
            e.fillInStackTrace();
            throw new NotSignUpException(ErrorCode.FIND_ID_ERROR);
        } catch (InternalServerException e){
            e.fillInStackTrace();
            throw new InternalServerException(ErrorCode.INTERNAL_SERVER_ERROR);
        } catch (SocialDataException e) {
            e.fillInStackTrace();
            throw new SocialDataException(ErrorCode.SOCIAL_ERROR);
        } catch (DuplicationException e){
            e.fillInStackTrace();
            throw new DuplicationException(ErrorCode.DUPLICATE_ERROR);
        } catch (NotMatchMemberIdException e){
            e.fillInStackTrace();
            throw new NotMatchMemberIdException(ErrorCode.MEMBERID_ERROR);
        }
    }
//-------------------------------------------------------- 회원정보수정 end --------------------------------------------------------

//-------------------------------------------------------- getIP start --------------------------------------------------------
    public static String getIp(HttpServletRequest request) {
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
//-------------------------------------------------------- getIP end --------------------------------------------------------

}
