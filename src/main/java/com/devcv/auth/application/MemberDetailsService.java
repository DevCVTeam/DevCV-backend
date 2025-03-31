package com.devcv.auth.application;

import com.devcv.auth.details.MemberDetails;
import com.devcv.common.exception.ErrorCode;
import com.devcv.member.domain.Member;
import com.devcv.member.exception.NotSignUpException;
import com.devcv.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class MemberDetailsService implements UserDetailsService {
    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            Member findMember =  memberRepository.findMemberByEmail(username);
            if (findMember == null){
                throw new NotSignUpException(ErrorCode.FIND_ID_ERROR);
            } else {
                List<GrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority("ROLE_"+findMember.getMemberRole().name()));
                return new MemberDetails(findMember, authorities);
            }
        } catch (NotSignUpException e){
            e.fillInStackTrace();
            throw new NotSignUpException(ErrorCode.FIND_ID_ERROR);
        }
    }

}
