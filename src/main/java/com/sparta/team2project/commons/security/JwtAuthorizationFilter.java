package com.sparta.team2project.commons.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.team2project.commons.entity.UserRoleEnum;
import com.sparta.team2project.commons.exceptionhandler.CustomException;
import com.sparta.team2project.commons.exceptionhandler.ErrorCode;
import com.sparta.team2project.commons.jwt.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static com.sparta.team2project.commons.jwt.JwtUtil.AUTHORIZATION_HEADER;


@Slf4j(topic = "JWT 검증 및 인가")
public class JwtAuthorizationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    public JwtAuthorizationFilter(JwtUtil jwtUtil, UserDetailsServiceImpl userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }


    // 헤더에 담아서 요청할 때
    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain filterChain) throws ServletException, IOException {

        // *** 이전과 다른부분, 쿠키에서 토큰을 추출하던 것에서 getJwtFromHeader()를 통해 헤더에서 순수한 토큰을 추출하는 것으로 변경 간결해짐.

        String accessToken = jwtUtil.resolveToken(req, jwtUtil.ACCESS_KEY);
        String refreshToken = jwtUtil.resolveToken(req, jwtUtil.REFRESH_KEY);
        log.info("dofilter 시작");
        if(accessToken != null) {
            if(jwtUtil.validateToken(accessToken)) {
                log.info("엑세스 유효");
                Claims accessInfo =jwtUtil.getUserInfoFromToken(accessToken);
                accessInfo.get(AUTHORIZATION_HEADER);
                setAuthentication(accessInfo.getSubject());
            } else if(refreshToken != null && jwtUtil.refreshTokenValidation(refreshToken)) {
                Claims refreshInfo = jwtUtil.getUserInfoFromToken(refreshToken);

                String roleString = (String)refreshInfo.get(AUTHORIZATION_HEADER);
                UserRoleEnum userRole = null;

                if (roleString.equals("USER")) {
                    userRole = UserRoleEnum.USER;
                } else if (roleString.equals("ADMIN")) {
                    userRole = UserRoleEnum.ADMIN;
                }

                String newAccessToken = jwtUtil.createToken(refreshInfo.getSubject(), "Access", userRole);
                jwtUtil.setHeaderAccessToken(res, newAccessToken);

                setAuthentication(refreshInfo.getSubject());
            } else if(refreshToken == null) {
                throw new CustomException(ErrorCode.EXPIRED_ACCESS_TOKEN);
            } else {
                throw new CustomException(ErrorCode.EXPIRED_REFRESH_TOKEN);
            }
        }
        filterChain.doFilter(req, res);
    }

    // 인증 처리
    public void setAuthentication(String email) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = createAuthentication(email);
        context.setAuthentication(authentication);

        SecurityContextHolder.setContext(context);
    }

    // 인증 객체 생성
    private Authentication createAuthentication(String email) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }
}