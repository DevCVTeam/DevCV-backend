package com.devcv.auth.presentation;

import com.devcv.auth.application.AuthService;
import com.devcv.auth.details.MemberDetails;
import com.devcv.auth.exception.JwtNotExpiredException;
import com.devcv.auth.exception.JwtNotFoundRefreshTokenException;
import com.devcv.auth.jwt.JwtProvider;
import com.devcv.auth.jwt.JwtTokenDto;
import com.devcv.common.exception.ErrorCode;
import com.devcv.common.exception.UnAuthorizedException;
import com.devcv.member.application.MemberService;
import com.devcv.member.domain.Member;
import com.devcv.member.domain.dto.MemberLoginRequest;
import com.devcv.member.domain.dto.MemberLoginResponse;
import com.devcv.member.domain.dto.MemberSignUpRequest;
import com.devcv.member.domain.dto.profile.GoogleProfile;
import com.devcv.member.domain.dto.profile.KakaoProfile;
import com.devcv.member.domain.enumtype.SocialType;
import com.devcv.member.exception.SocialLoginException;
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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/members/*")
@PropertySource("classpath:application.yml")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final MemberService memberService;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    @Value("${keys.jwtkey}")
    private String jwtkey;
    @Value("${keys.social_password}")
    private String socialPassword;
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String AUTHORIZATION_REFRESH_HEADER = "RefreshToken";
    public static final String BEARER_PREFIX = "Bearer ";
//-------------------------------------------------------- 회원가입 start --------------------------------------------------------
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody MemberSignUpRequest memberSignUpRequest) {
        // JWT 복호화
        MemberSignUpRequest memberSignUpRequestAuth = memberSignUpRequest.toBuilder()
                .password(String.valueOf(jwtProvider.parseClaims(memberSignUpRequest.getPassword()).get("password"))).build();
        authService.signup(memberSignUpRequestAuth);
        return ResponseEntity.ok().build();
    }
//-------------------------------------------------------- 회원가입 end --------------------------------------------------------
//-------------------------------------------------------- 로그인 start --------------------------------------------------------
    @PostMapping("/login")
    public ResponseEntity<MemberLoginResponse> memberLogin(@RequestBody MemberLoginRequest memberLoginRequest) {
        // JWT 복호화
        MemberLoginRequest memberLoginRequestAuth = memberLoginRequest.toBuilder()
                .password(String.valueOf(jwtProvider.parseClaims(memberLoginRequest.getPassword()).get("password"))).build();
        MemberLoginResponse resultResponse = authService.login(memberLoginRequestAuth);
        ResponseCookie responseCookie = ResponseCookie.from(AUTHORIZATION_REFRESH_HEADER,resultResponse.getRefreshToken())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(60*60*24*7)
                .sameSite("None")
                .build();
        return ResponseEntity.ok().header(AUTHORIZATION_HEADER,BEARER_PREFIX + resultResponse.getAccessToken())
                .header(HttpHeaders.SET_COOKIE, String.valueOf(responseCookie)).body(resultResponse);
    }
//-------------------------------------------------------- 로그인 end --------------------------------------------------------
//-------------------------------------------------------- 로그아웃 start --------------------------------------------------------
    @PostMapping("/logout")
    public ResponseEntity<String> memberLogout(){
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        Cookie[] cookies = request.getCookies();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        MemberDetails memberDetails = (MemberDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if( authentication != null && cookies != null) {
            SecurityContextHolder.clearContext();
            for(Cookie cookie : cookies){
                cookie.setMaxAge(0);
            }
        }
        memberService.updateRefreshTokenBymemberId(memberDetails.getMember().getMemberId(), null);
        ResponseCookie responseCookie = ResponseCookie.from(AUTHORIZATION_REFRESH_HEADER, "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(60*60*24*7)
                .sameSite("None")
                .build();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, String.valueOf(responseCookie)).build();
    }
//-------------------------------------------------------- 로그아웃 end --------------------------------------------------------
//-------------------------------------------------------- 카카오로그인 start --------------------------------------------------------
    @GetMapping("/kakao-login")
    public ResponseEntity<Object> memberAuthKakao(@RequestParam String token){
        ObjectMapper objectMapper = new ObjectMapper();
        RestTemplate restTemplateInfo = new RestTemplate();
        HttpHeaders headersInfo = new HttpHeaders();
        headersInfo.add("Authorization", "Bearer " + token);
        headersInfo.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        HttpEntity<MultiValueMap<String, String>> kakaoProfileRequest = new HttpEntity<>(headersInfo);

        // Http POST && Response
        ResponseEntity<String> responseInfo = restTemplateInfo.exchange("https://kapi.kakao.com/v2/user/me", HttpMethod.POST,
                kakaoProfileRequest, String.class
        );

        // KaKaoProfile 객체
        KakaoProfile profile;
        try {
            profile = objectMapper.readValue(responseInfo.getBody(), KakaoProfile.class);
        } catch (JsonProcessingException e) {
            e.fillInStackTrace();
            throw new UnAuthorizedException(ErrorCode.UNAUTHORIZED_ERROR);
        }

        // 이미 가입되어 있는 이메일이면 해당 이메일로 로그인
        Member findMember = memberService.findMemberByEmail(profile.getKakao_account().getEmail());
        if(findMember != null){
            // 해당 소셜 이메일이 이미 일반계정으로 가입되어 있는 경우
            if(!passwordEncoder.matches(socialPassword, findMember.getPassword())){
                throw new SocialLoginException(ErrorCode.SOCIAL_LOGIN_ERROR);
            }
            // 로그인 진행
            MemberLoginRequest memberLoginRequest =MemberLoginRequest.builder().email(profile.getKakao_account().getEmail()).password(socialPassword).build();
            return getObjectResponseEntity(memberLoginRequest);
        } else { // 가입되어있지 않는 이메일이면 회원가입페이지로 이메일 정보 넘김.
            long now = (new Date()).getTime();
            Map<String,Object> userInfo = new HashMap<>(){{
                put("nickName",profile.getProperties().getNickname());
                put("email",profile.getKakao_account().getEmail());
                put("social", SocialType.kakao.name());
                put("accessToken", Jwts.builder()
                        .claim("nickName",profile.getProperties().getNickname())
                        .claim("email",profile.getKakao_account().getEmail())
                        .claim("social",SocialType.kakao.name())
                        .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtkey)), SignatureAlgorithm.HS512)
                        .setExpiration(new Date(now + 1000 * 60 * 3)) // 3분
                        .compact());
            }};
            ResponseCookie responseCookie = ResponseCookie.from(AUTHORIZATION_REFRESH_HEADER, Jwts.builder()
                            .setExpiration(new Date(now + 1000 * 60 * 3))
                            .claim("email", profile.getKakao_account().getEmail())            // payload "email": "testemail@test.com" (ex)
                            .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtkey)), SignatureAlgorithm.HS512)
                            .compact())
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .maxAge(60*60*24*7)
                    .sameSite("None")
                    .build();
            return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE,String.valueOf(responseCookie)).body(userInfo);
        }
    }
//-------------------------------------------------------- 카카오로그인 end --------------------------------------------------------

//-------------------------------------------------------- 구글로그인 start --------------------------------------------------------
    @GetMapping("/google-login")
    public ResponseEntity<Object> memberAuthGoogle(@RequestParam String token){

        RestTemplate restTemplateInfo = new RestTemplate();
        HttpHeaders headersInfo = new HttpHeaders();
        headersInfo.add("Authorization", "Bearer " + token);
        headersInfo.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
        HttpEntity<MultiValueMap<String, String>> googleProfileRequest = new HttpEntity<>(headersInfo);

        // Http POST && Response
        ResponseEntity<String> responseInfo = restTemplateInfo.exchange("https://www.googleapis.com/userinfo/v2/me", HttpMethod.GET,
                googleProfileRequest, String.class
        );
        ObjectMapper objectMapper = new ObjectMapper();
        GoogleProfile googleProfile;
        try {
            googleProfile = objectMapper.readValue(responseInfo.getBody(), GoogleProfile.class);
        } catch (JsonProcessingException e) {
            e.fillInStackTrace();
            throw new UnAuthorizedException(ErrorCode.UNAUTHORIZED_ERROR);
        }

        // 이미 가입되어 있는 이메일이면 해당 이메일로 로그인
        Member findMember = memberService.findMemberByEmail(googleProfile.getEmail());
        if(findMember != null){
            // 해당 소셜 이메일이 이미 일반계정으로 가입되어 있는 경우
            if(!passwordEncoder.matches(socialPassword, findMember.getPassword())){
                throw new SocialLoginException(ErrorCode.SOCIAL_LOGIN_ERROR);
            }
            // 로그인 진행.
            MemberLoginRequest memberLoginRequest = MemberLoginRequest.builder()
                    .email(googleProfile.getEmail()).password(socialPassword).build();
            return getObjectResponseEntity(memberLoginRequest);
        } else { // 가입되어있지 않는 이메일이면 회원가입페이지로 이메일 정보 넘김.
            long now = (new Date()).getTime();
            Map<String,Object> userInfo = new HashMap<>(){{
                put("nickName",googleProfile.getName());
                put("email",googleProfile.getEmail());
                put("social",SocialType.google.name());
                put("accessToken", Jwts.builder()
                        .claim("nickName",googleProfile.getName())
                        .claim("email",googleProfile.getEmail())
                        .claim("social",SocialType.google.name())
                        .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtkey)), SignatureAlgorithm.HS512)
                        .setExpiration(new Date(now + 1000 * 60 * 3)) // 3분
                        .compact());
            }};
            ResponseCookie responseCookie = ResponseCookie.from(AUTHORIZATION_REFRESH_HEADER, Jwts.builder()
                            .setExpiration(new Date(now + 1000 * 60 * 3))
                            .claim("email", googleProfile.getEmail())            // payload "email": "testemail@test.com" (ex)
                            .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtkey)), SignatureAlgorithm.HS512)
                            .compact())
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .maxAge(60*60*24*7)
                    .sameSite("None")
                    .build();
            return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE,String.valueOf(responseCookie)).body(userInfo);
        }
    }
//-------------------------------------------------------- 구글로그인 end --------------------------------------------------------
    // 로그인 과정 응답 메서드 카카오로그인 & 구글로그인 공통.
    private ResponseEntity<Object> getObjectResponseEntity(MemberLoginRequest memberLoginRequest) {
        MemberLoginResponse memberLoginResponse = authService.login(memberLoginRequest);
        ResponseCookie responseCookie = ResponseCookie.from(AUTHORIZATION_REFRESH_HEADER, memberLoginResponse.getRefreshToken())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(60*60*24*7)
                .sameSite("None")
                .build();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE,String.valueOf(responseCookie)).body(memberLoginResponse);
    }
//-------------------------------------------------------- Token재발급 start --------------------------------------------------------
    @GetMapping("/refresh-token")
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public ResponseEntity<Map<String,Object>> refreshAccessToken(@CookieValue(value = "RefreshToken", required = false) Cookie refreshTokenCookie) {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            String accessToken = request.getHeader(AUTHORIZATION_HEADER).split(" ")[1];
            MemberDetails memberDetails = (MemberDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            Member findMember = memberService.findMemberBymemberId(memberDetails.getMember().getMemberId());
            if (StringUtils.hasText(refreshTokenCookie.getValue())) {
                // refreshToken 유효성검사.
                if (jwtProvider.validateToken(refreshTokenCookie.getValue()) && !jwtProvider.validateToken(accessToken)
                && refreshTokenCookie.getValue().equals(findMember.getRefreshToken())) {
                    // 검사완료되면 accessToken 재발급
                    String email = String.valueOf(jwtProvider.parseClaims(refreshTokenCookie.getValue()).get("email"));
                    JwtTokenDto jwtTokenDto = jwtProvider.refreshTokenDto(email, refreshTokenCookie.getValue());
                    // RefreshToken Cookie에 담기.
                    ResponseCookie responseCookie = ResponseCookie.from(AUTHORIZATION_REFRESH_HEADER, refreshTokenCookie.getValue())
                            .httpOnly(true)
                            .secure(false)
                            .path("/")
                            .domain("ec2-100-26-178-217.compute-1.amazonaws.com")
                            .maxAge(60*60*24*7)
                            .sameSite("None")
                            .build();
                    // AccessToken body에 담아 응답.
                    Map<String, Object> accessTokenInfo = new HashMap<>() {{
                        put("accessToken", jwtTokenDto.getAccessToken());
                    }};
                    return ResponseEntity.ok().header(AUTHORIZATION_HEADER, BEARER_PREFIX + jwtTokenDto.getAccessToken())
                            .header(HttpHeaders.SET_COOKIE, responseCookie.toString()).body(accessTokenInfo);
                } else {
                    throw new JwtNotExpiredException(ErrorCode.JWT_NOT_EXPIRED_ERROR);
                }
            } else {
                throw new JwtNotFoundRefreshTokenException(ErrorCode.REFRESHTOKEN_NOT_FOUND);
            }
        } catch (NullPointerException | JwtNotFoundRefreshTokenException e){
            e.fillInStackTrace();
            throw new JwtNotFoundRefreshTokenException(ErrorCode.REFRESHTOKEN_NOT_FOUND);
        } catch (JwtNotExpiredException e){
            e.fillInStackTrace();
            throw new JwtNotExpiredException(ErrorCode.JWT_NOT_EXPIRED_ERROR);
        }
    }
//-------------------------------------------------------- Token재발급 end --------------------------------------------------------
}
