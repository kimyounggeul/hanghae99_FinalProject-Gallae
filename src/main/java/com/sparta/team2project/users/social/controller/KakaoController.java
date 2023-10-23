package com.sparta.team2project.users.social.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sparta.team2project.commons.dto.MessageResponseDto;
import com.sparta.team2project.commons.jwt.JwtUtil;
import com.sparta.team2project.commons.security.UserDetailsServiceImpl;
import com.sparta.team2project.users.UserService;
import com.sparta.team2project.users.social.service.KakaoService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class KakaoController {
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;
    private final KakaoService kakaoService;

    @Operation(summary = "카카오 회원가입입니다", description = "카카오 회원가입 api 입니다")
    @GetMapping("/kakao/callback")
    public ResponseEntity<String> kakaoLogin(@RequestParam String code, HttpServletResponse response) throws JsonProcessingException {
        // 리프레시 토큰을 받기 위해 tokenDto 생성
        String[] tokenArray = kakaoService.kakaoLogin(code, response);

        // code: 카카오 서버로부터 받은 인가 코드
        String createAccessToken = tokenArray[0];
        String createRefreshToken = tokenArray[1];

        // 헤더로 바꿔야함!
        response.addHeader("ACCESS_KEY", createAccessToken);
        response.addHeader("REFRESH_KEY", createRefreshToken);

        return ResponseEntity.ok("성공");
    }
}
