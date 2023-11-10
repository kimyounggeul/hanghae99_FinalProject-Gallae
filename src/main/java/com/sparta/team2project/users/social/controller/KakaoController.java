package com.sparta.team2project.users.social.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sparta.team2project.commons.Util.JwtUtil;
import com.sparta.team2project.commons.dto.MessageResponseDto;
import com.sparta.team2project.commons.security.UserDetailsServiceImpl;
import com.sparta.team2project.refreshToken.TokenDto;
import com.sparta.team2project.users.UserService;
import com.sparta.team2project.users.dto.LoginRequestDto;
import com.sparta.team2project.users.social.dto.KakaoUserInfoDto;
import com.sparta.team2project.users.social.dto.ResponseDto;
import com.sparta.team2project.users.social.service.KakaoService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
//@RequestMapping("/api/users")
public class KakaoController {
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;
    private final KakaoService kakaoService;

    @Operation(summary = "카카오 회원가입입니다", description = "카카오 회원가입 api 입니다")
//    @GetMapping("/kakao/callback")
//    public String kakaoLogin(@RequestParam String code, HttpServletResponse response) throws JsonProcessingException {
//        // 리프레시 토큰을 받기 위해 tokenDto 생성
//        return kakaoService.kakaoLogin(code,response);
//    }
// 카카오 아이디
@GetMapping("/kakao/callback")
public ResponseDto<KakaoUserInfoDto> kakaoLogin(@RequestParam String code, HttpServletResponse response) throws JsonProcessingException {
    return kakaoService.kakaoLogin(code, response);
}
}