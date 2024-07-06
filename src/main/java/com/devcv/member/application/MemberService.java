package com.devcv.member.application;

import com.devcv.common.exception.ErrorCode;
import com.devcv.member.domain.Member;
import com.devcv.member.exception.NotSignUpException;
import com.devcv.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/*
*   ********************************* MemberService 하위 메서드 기능 정리 *********************************
*
*       ************************************ SELECT ************************************
*       findMemberByEmail : email로 해당 회원정보 SELECT.
*       findMemberBymemberId : memberId로 해당 회원정보 SELECT.
*       findMemberBymemberNameAndPhone: 이름&전화번호 정보로 해당 회원정보 SELECT.
*       ************************************ SELECT ************************************
*
*       ************************************ INSERT ************************************
*       signup : 회원가입 INSERT.
*       ************************************ INSERT ************************************
*
*       ************************************ UPDATE ************************************
*       updatePasswordBymemberId: memberId로 해당 회원 비밀번호 UPDATE.
*       updateMemberBymemberId: memberId로 해당 회원정보 UPDATE.
*       ************************************ UPDATE ************************************
*
*   ********************************* MemberService 하위 메서드 기능 정리 *********************************
 */
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    public Member findMemberByEmail(String email) {
       return memberRepository.findMemberByEmail(email);
    }
    public Member findMemberBymemberId(Long memberId) {
        try {
            Member findMember = memberRepository.findMemberBymemberId(memberId);
            if(findMember == null){
                throw new NotSignUpException(ErrorCode.MEMBER_NOT_FOUND);
            } else {
                return findMember;
            }
        } catch (NotSignUpException e){
            e.fillInStackTrace();
            throw new NotSignUpException(ErrorCode.MEMBER_NOT_FOUND);
        }
    }
    public List<Member> findMemberBymemberNameAndPhone(String memberName, String phone) {
        return memberRepository.findMemberBymemberNameAndPhone(memberName,phone);
    }
    @Transactional
    public int updatePasswordBymemberId(String password,Long memberId) {
        return memberRepository.updatePasswordBymemberId(password,memberId);
    }
    @Transactional
    public int updateMemberBymemberId(String memberName, String email, String password, String nickname, String phone, String address,
                                      String company, String job, String stack, Long memberId) {
        return memberRepository.updateMemberBymemberId(memberName,email,password,nickname,phone,address,company,job,stack,memberId);}
    @Transactional
    public int updateSocialMemberBymemberId(String memberName, String nickname, String phone, String address,
                                            String company, String job, String stack, Long memberId){
        return memberRepository.updateSocialMemberBymemberId(memberName,nickname,phone,address,company,job,stack,memberId);
    }
    @Transactional
    public int updateRefreshTokenBymemberId(Long memberId, String refreshToken){
        return memberRepository.updateRefreshTokenBymemberId(memberId,refreshToken);
    }
}
