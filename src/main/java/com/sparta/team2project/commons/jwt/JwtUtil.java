package com.sparta.team2project.commons.jwt;

import com.sparta.team2project.commons.entity.UserRoleEnum;
import com.sparta.team2project.refreshToken.RefreshToken;
import com.sparta.team2project.refreshToken.RefreshTokenRepository;
import com.sparta.team2project.refreshToken.TokenDto;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;

@Slf4j(topic = "JwtUtil")
@Component
@RequiredArgsConstructor
public class JwtUtil {
    public static final String AUTHORIZATION_HEADER = "Authorization"; // 헤더의 키값
//    public static final String AUTHORIZATION_KEY = "auth"; // 토큰의 키값
    public static final String BEARER_PREFIX = "Bearer "; // 토큰 접두사
    public static final String ACCESS_KEY = "ACCESS_KEY";
    public static final String REFRESH_KEY = "REFRESH_KEY";


    private final long ACCESS_TOKEN_TIME = 30  * 1000L; //
    private final long REFRESH_TOKEN_TIME = 2 * 7 * 24 * 60 * 60 * 1000L; // 14일 설정

    @Value("${jwt.secret.key}")
    private String secretKey;
    private Key key;
    private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

    private final RefreshTokenRepository refreshTokenRepository;


    @PostConstruct// 객체 생성 후 초기화를 수행하는 메서드
    public void init() {
        byte[] bytes = Base64.getDecoder().decode(secretKey);
        key = Keys.hmacShaKeyFor(bytes);
    }

    public String resolveToken(HttpServletRequest request, String tokentype) {

        String tokenType = tokentype.equals("ACCESS_KEY") ? ACCESS_KEY : REFRESH_KEY;
        String bearerToken = request.getHeader(tokenType);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(7); // bearer과 공백 제거
        }
        return null;
    }

    public TokenDto createAllToken(String email, UserRoleEnum userRole) {

        return new TokenDto(createToken(email, "Access", userRole), createToken(email, "Refresh", userRole));
    }

    //토큰 생성 (email 기준! )
    public String createToken(String username, String tokentype, UserRoleEnum userRole) {

        Date date = new Date();
        System.out.println("토큰 발급 시작");
        String role = userRole.toString();
        long expireTime = tokentype.equals("Access") ? ACCESS_TOKEN_TIME : REFRESH_TOKEN_TIME;

        String jwToken =  BEARER_PREFIX +
                Jwts.builder()
                        .setSubject(username)
                        .claim(AUTHORIZATION_HEADER, role)
                        .setExpiration(new Date(date.getTime() + expireTime))
                        .setIssuedAt(date)
                        .signWith(key, signatureAlgorithm)
                        .compact();
        System.out.println(jwToken);
        return jwToken;
    }

    // 액세스 토큰 만료시 리프레시토큰 확인
    public Boolean refreshTokenValidation(String token) {

        if(!validateToken(token)) {
            return false;
        }

        Claims info = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        Optional<RefreshToken> refreshToken = refreshTokenRepository.findByEmail(info.getSubject());
        return refreshToken.isPresent() && token.equals(refreshToken.get().getRefreshToken());

    }

//
//    //header 에서 token 추출
//    public String getJwtFromHeader(HttpServletRequest request) {
//        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
//        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
//            return bearerToken.substring(7);
//        }
//        return null;
//    }

    // 토큰 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException | SignatureException e) {
            log.error("Invalid JWT signature, 유효하지 않는 JWT 서명 입니다.");
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token, 만료된 JWT token 입니다.");
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token, 지원되지 않는 JWT 토큰 입니다.");
        } catch (IllegalArgumentException e) {
            log.error("JWT claims is empty, 잘못된 JWT 토큰 입니다.");
        }
        return false;
    }
//    // 리프레시 토큰인지 확인
//    public boolean isRefreshToken(String tokenValue) {
//        Claims claims = this.getUserInfoFromToken(tokenValue);
//        return claims.getExpiration().getTime() > ACCESS_TOKEN_TIME;
//    }

    // 토큰에서 사용자 정보 가져오기
    public Claims getUserInfoFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    public void setHeaderAccessToken(HttpServletResponse response, String accessToken) {

        response.setHeader(ACCESS_KEY, accessToken);
    }
}