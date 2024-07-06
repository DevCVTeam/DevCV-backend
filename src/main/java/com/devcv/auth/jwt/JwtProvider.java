package com.devcv.auth.jwt;


import com.devcv.auth.details.MemberDetails;
import com.devcv.auth.exception.JwtIllegalArgumentException;
import com.devcv.auth.exception.JwtInvalidSignException;
import com.devcv.auth.exception.JwtUnsupportedException;
import com.devcv.common.exception.ErrorCode;
import com.devcv.member.domain.Member;
import com.devcv.member.domain.enumtype.RoleType;
import com.devcv.member.domain.enumtype.SocialType;
import com.devcv.member.exception.NotSignUpException;
import com.devcv.member.repository.MemberRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtProvider {
    private static final String ROLE_TYPE = "role";
    private static final String PK_VALUE = "memberId";
    private static final String MEMBER_NAME = "memberName";
    private static final String MEMBER_EMAIL = "email";
    private static final String SOCIAL_TYPE = "social";
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String AUTHORIZATION_REFRESH_HEADER = "RefreshToken";
    private static final String BEARER_TYPE = "Bearer";
    private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 60;            // 60분
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24 * 7;  // 7일
    private final Key key;
    private final MemberRepository memberRepository;


    public JwtProvider (@Value("${keys.jwtkey}") String secretKey, MemberRepository memberRepository){
        this.memberRepository = memberRepository;
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }


    public JwtTokenDto generateTokenDto(Authentication authentication) {
        // 권한들 가져오기
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        MemberDetails memberDetails = (MemberDetails) authentication.getPrincipal();
        long now = (new Date()).getTime();
        // Access Token 생성
        Date accessTokenExpiresIn = new Date(now + ACCESS_TOKEN_EXPIRE_TIME);
        String accessToken = Jwts.builder()
                .claim(PK_VALUE, memberDetails.getMember().getMemberId())              // payload "memberId": "name" (ex)
                .claim(ROLE_TYPE, authorities.split("_")[1])                    // payload "role": "일반" (ex)
                .claim(SOCIAL_TYPE, memberDetails.getMember().getSocial().name())     // payload "social": "일반" (ex)
                .claim(MEMBER_NAME, memberDetails.getMember().getMemberName())        // payload "memberName": "홍길동" (ex)
                .claim(MEMBER_EMAIL, memberDetails.getMember().getEmail())            // payload "email": "testemail@test.com" (ex)
                .setExpiration(accessTokenExpiresIn)                                  // payload "exp": 151621022 (ex)
                .signWith(key, SignatureAlgorithm.HS512)                              // header "alg": "HS512"
                .compact();

        // Refresh Token 생성
        String refreshToken = Jwts.builder()
                .setExpiration(new Date(now + REFRESH_TOKEN_EXPIRE_TIME))
                .claim(MEMBER_EMAIL, memberDetails.getMember().getEmail())            // payload "email": "testemail@test.com" (ex)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();

        return JwtTokenDto.builder()
                .grantType(BEARER_TYPE)
                .accessToken(accessToken)
                .accessTokenExpiresIn(accessTokenExpiresIn.getTime())
                .refreshToken(refreshToken)
                .build();
    }

    public JwtTokenDto refreshTokenDto(String email, String refreshToken){
        long now = (new Date()).getTime();
        Date accessTokenExpiresIn = new Date(now + ACCESS_TOKEN_EXPIRE_TIME);
        try {
            Member findMember = memberRepository.findMemberByEmail(email);
            if(findMember == null){
                throw new NotSignUpException(ErrorCode.FIND_ID_ERROR);
            }
            String accessToken = Jwts.builder()
                    .claim(PK_VALUE, findMember.getMemberId())              // payload "memberId": "name" (ex)
                    .claim(ROLE_TYPE, findMember.getMemberRole())                    // payload "role": "일반" (ex)
                    .claim(SOCIAL_TYPE, findMember.getSocial())     // payload "social": "일반" (ex)
                    .claim(MEMBER_NAME, findMember.getMemberName())        // payload "memberName": "홍길동" (ex)
                    .claim(MEMBER_EMAIL, findMember.getEmail())            // payload "email": "testemail@test.com" (ex)
                    .setExpiration(accessTokenExpiresIn)                                  // payload "exp": 151621022 (ex)
                    .signWith(key, SignatureAlgorithm.HS512)                              // header "alg": "HS512"
                    .compact();
            return JwtTokenDto.builder()
                    .grantType(BEARER_TYPE)
                    .accessToken(accessToken)
                    .accessTokenExpiresIn(accessTokenExpiresIn.getTime())
                    .refreshToken(refreshToken)
                    .build();
        } catch (NotSignUpException e){
            e.fillInStackTrace();
            throw new NotSignUpException(ErrorCode.FIND_ID_ERROR);
        }
    }

    public Authentication getAuthentication(String accessToken) {
        // 토큰 복호화
        Claims claims = parseClaims(accessToken);

        if (claims.get(ROLE_TYPE) == null) {
            throw new RuntimeException("권한 정보가 없는 토큰입니다.");
        }
        String memberRole = "ROLE_" + claims.get(ROLE_TYPE);
        // 클레임에서 권한 정보 가져오기
        List<? extends GrantedAuthority> authorities =
                Arrays.stream(memberRole.split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
        // MemberDetails 객체를 만들어서 Authentication 리턴
        MemberDetails principal = new MemberDetails(Member.builder().memberRole(RoleType.valueOf(claims.get(ROLE_TYPE).toString()))
                .social(SocialType.valueOf(claims.get(SOCIAL_TYPE).toString()))
                .memberName(claims.get(MEMBER_NAME).toString())
                .email(claims.get(MEMBER_EMAIL).toString())
                .memberId(Long.parseLong(claims.get(PK_VALUE).toString()))
                .password("Principalpassword")
                .build(), authorities);

        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        }  catch (ExpiredJwtException e) {
            log.error("만료된 JWT 토큰입니다.");
            return false;
        }
    }

    public Claims parseClaims(String accessToken) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }
    public String resolveAccessToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_TYPE)) {
            return bearerToken.split(" ")[1].trim();
        }
        return null;
    }
    public String resolveRefreshToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_REFRESH_HEADER);
        System.out.println(Arrays.toString(request.getCookies()));
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_TYPE)) {
            return bearerToken.split(" ")[1].trim();
        }
        return null;
    }
}
