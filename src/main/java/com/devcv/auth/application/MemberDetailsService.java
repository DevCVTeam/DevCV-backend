package com.devcv.auth.application;

import com.devcv.common.exception.ErrorCode;
import com.devcv.member.domain.Member;
import com.devcv.member.exception.NotSignUpException;
import com.devcv.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;


@Service
@RequiredArgsConstructor
public class MemberDetailsService implements UserDetailsService {
    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try{
            Member findMember =  memberRepository.findMemberByEmail(username);
            if(findMember == null){
                throw new NotSignUpException(ErrorCode.FIND_ID_ERROR);
            } else {
                return createUserDetails(findMember);
            }
        } catch (NotSignUpException ne){
            throw new NotSignUpException(ErrorCode.FIND_ID_ERROR);
        }
    }
    // DB 에 User 값이 존재 -> UserDetails 리턴
    private UserDetails createUserDetails(Member member) {
        GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(member.getMemberRole().name());

        return new User(
                String.valueOf(member.getMemberId()),
                member.getPassword(),
                Collections.singleton(grantedAuthority)
        );
    }
}