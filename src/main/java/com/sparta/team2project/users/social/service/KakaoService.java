package com.sparta.team2project.users.social.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.team2project.commons.entity.UserRoleEnum;
import com.sparta.team2project.commons.Util.JwtUtil;
import com.sparta.team2project.refreshToken.TokenDto;
import com.sparta.team2project.users.UserRepository;
import com.sparta.team2project.users.Users;
import com.sparta.team2project.users.social.dto.KakaoUserInfoDto;
import com.sparta.team2project.users.social.dto.ResponseDto;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j(topic = "Kakao Login")
@Service
@RequiredArgsConstructor
public class KakaoService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${kakaoClientId}")
    private String kakaoClientId;
    @Value("${kakaoRedirectUri}")
    private String kakaoRedirectUri;

//    public String kakaoLogin(String code, HttpServletResponse response) throws JsonProcessingException {
//        // 1. "인가 코드"로 "액세스 토큰" 요청
//        String accessAuthorizationToken = getToken(code);
//        // 2. 토큰으로 카카오 API 호출 : "액세스 토큰"으로 "카카오 사용자 정보" 가져오기
//        KakaoUserInfoDto kakaoUserInfo = getKakaoUserInfo(accessAuthorizationToken);
//        // 3. 필요시에 회원가입
//        Users kakaoUser = registerKakaoUserIfNeeded(kakaoUserInfo);
//        // 4. JWT 토큰 반환
//        String accessToken = jwtUtil.createAccessToken(kakaoUser.getEmail(), kakaoUser.getUserRole());
//        String refreshToken = jwtUtil.createRefreshToken(kakaoUser.getEmail(), kakaoUser.getUserRole());
//
//        response.addHeader(JwtUtil.ACCESS_KEY, accessToken);
//        response.addHeader(JwtUtil.REFRESH_KEY, refreshToken);
//
//        return "redirect:/";
//    }
//
//    private String getToken(String code) throws JsonProcessingException {
//        // 요청 URL 만들기
//        URI uri = UriComponentsBuilder
//                .fromUriString("https://kauth.kakao.com")
//                .path("/oauth/token")
//                .encode()
//                .build()
//                .toUri();
//
//        // HTTP Header 생성
//        HttpHeaders headers = new HttpHeaders();
//        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
//
//        // HTTP Body 생성
//        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
//        body.add("grant_type", "authorization_code");
//        body.add("client_id", kakaoClientId);
//        body.add("redirect_uri", kakaoRedirectUri);
//        body.add("code", code);
//
//        RequestEntity<MultiValueMap<String, String>> requestEntity = RequestEntity
//                .post(uri)
//                .headers(headers)
//                .body(body);
//
//        // HTTP 요청 보내기
//        ResponseEntity<String> response = restTemplate.exchange(
//                requestEntity,
//                String.class
//        );
//
//        // HTTP 응답 (JSON) -> 액세스 토큰 파싱
//        JsonNode jsonNode = new ObjectMapper().readTree(response.getBody());
//        return jsonNode.get("access_token").asText();
//    }
//
//    private KakaoUserInfoDto getKakaoUserInfo(String accessToken) throws JsonProcessingException {
//        // 요청 URL 만들기
//        URI uri = UriComponentsBuilder
//                .fromUriString("https://kapi.kakao.com")
//                .path("/v2/user/me")
//                .encode()
//                .build()
//                .toUri();
//
//        // HTTP Header 생성
//        HttpHeaders headers = new HttpHeaders();
//        headers.add("Authorization", "Bearer " + accessToken);
//        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
//
//        RequestEntity<MultiValueMap<String, String>> requestEntity = RequestEntity
//                .post(uri)
//                .headers(headers)
//                .body(new LinkedMultiValueMap<>());
//
//        // HTTP 요청 보내기
//        ResponseEntity<String> response = restTemplate.exchange(
//                requestEntity,
//                String.class
//        );
//
//        JsonNode jsonNode = new ObjectMapper().readTree(response.getBody());
//        Long id = jsonNode.get("id").asLong();
//        String nickname = jsonNode.get("properties")
//                .get("nickname").asText();
//        String email = jsonNode.get("kakao_account")
//                .get("email").asText();
//
//        log.info("카카오 사용자 정보: " + id + ", " + nickname + ", " + email);
//        return new KakaoUserInfoDto(id, nickname, email);
//    }
//
//    private Users registerKakaoUserIfNeeded(KakaoUserInfoDto kakaoUserInfo) {
//        Long kakaoId = kakaoUserInfo.getId();
//        Users kakaoUser = userRepository.findByKakaoId(kakaoId).orElse(null);
//
//        if (kakaoUser == null) {
//            // 일반 유저 중 같은 이메일 확인하기
//            String kakaoEmail = kakaoUserInfo.getEmail();
//            String nickname = kakaoUserInfo.getNickname();
//
//            Users sameEmailUser = userRepository.findByEmail(kakaoEmail).orElse(null);
//            // 일반 가입이 되어있는 경우
//            if (sameEmailUser != null) {
//                kakaoUser = sameEmailUser;
//                kakaoUser = kakaoUser.kakaoIdUpdate(kakaoId);
//            } else {
//                // 신규 회원
//                String password = UUID.randomUUID().toString();
//                String encodedPassword = passwordEncoder.encode(password);
//
//                kakaoUser = new Users(kakaoEmail, nickname, encodedPassword, kakaoId);
//                userRepository.save(kakaoUser);
//            }
//        }
//        return kakaoUser;
//    }
public ResponseDto<KakaoUserInfoDto> kakaoLogin(String code, HttpServletResponse response) throws JsonProcessingException {
    // 1. "인가 코드"로 "액세스 토큰" 요청
    String accessToken = getToken(code);

    // 2. 토큰으로 카카오 API 호출 : "액세스 토큰"으로 "카카오 사용자 정보" 가져오기
    KakaoUserInfoDto kakaoUserInfo = getKakaoUserInfo(accessToken);

    // 3. 필요시에 회원가입
    Users kakaoUser = registerKakaoUserIfNeeded(kakaoUserInfo);
    kakaoUserInfo.setNickname(kakaoUser.getNickName());

    // 4. JWT 토큰 반환
    String createToken =  jwtUtil.createToken(kakaoUser.getEmail(), UserRoleEnum.USER, 60 * 60 *1000L);

    // RefreshToken 생성
    String createRefreshToken = jwtUtil.createToken(kakaoUserInfo.getEmail(), UserRoleEnum.USER, 14 * 24 * 60 * 60 *1000L);

    // TTL 세팅과 함께 새 토큰으로 업데이트 및 저장
    redisTemplate.opsForValue().set("RT:" + kakaoUser.getEmail(), createRefreshToken, 14 * 24 * 60 * 60 *1000L, TimeUnit.MILLISECONDS);

    response.addHeader(JwtUtil.ACCESS_KEY, createToken);
    response.addHeader(JwtUtil.REFRESH_KEY, createRefreshToken);

    return ResponseDto.setSuccess("카카오 로그인 성공", kakaoUserInfo);
}

    // 1. "인가 코드"로 "액세스 토큰" 요청
    private String getToken(String code) throws JsonProcessingException {
        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP Body 생성
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", kakaoClientId);
        body.add("redirect_uri", kakaoRedirectUri);
        body.add("code", code);

        // HTTP 요청 보내기
        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest =
                new HttpEntity<>(body, headers);
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.exchange(
                "https://kauth.kakao.com/oauth/token",
                HttpMethod.POST,
                kakaoTokenRequest,
                String.class
        );

        // HTTP 응답 (JSON) -> 액세스 토큰 파싱
        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        return jsonNode.get("access_token").asText();
    }

    // 2. 토큰으로 카카오 API 호출 : "액세스 토큰"으로 "카카오 사용자 정보" 가져오기
    private KakaoUserInfoDto getKakaoUserInfo(String accessToken) throws JsonProcessingException {
        // HTTP Header 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HTTP 요청 보내기
        HttpEntity<MultiValueMap<String, String>> kakaoUserInfoRequest = new HttpEntity<>(headers);
        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.POST,
                kakaoUserInfoRequest,
                String.class
        );

        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);
        Long id = jsonNode.get("id").asLong();
        String nickname = jsonNode.get("properties")
                .get("nickname").asText();
        String email = jsonNode.get("kakao_account")
                .get("email").asText();

        log.info("카카오 사용자 정보: " + id + ", " + nickname + ", " + email);
        return new KakaoUserInfoDto(id, nickname, email);
    }

    // 3. 필요시에 회원가입
    public Users registerKakaoUserIfNeeded(KakaoUserInfoDto kakaoUserInfo) {
        // DB 에 중복된 Kakao Id 가 있는지 확인
        Long kakaoId = kakaoUserInfo.getId();
        Users kakaoUser = userRepository.findByKakaoId(kakaoId)
                .orElse(null);
        if (kakaoUser == null) {
            // 카카오 사용자 email 동일한 email 가진 회원이 있는지 확인
            String kakaoEmail = kakaoUserInfo.getEmail();
            Users sameEmailUser = userRepository.findByEmail(kakaoEmail).orElse(null);
            if (sameEmailUser != null) {
                kakaoUser = sameEmailUser;
                // 기존 회원정보에 카카오 Id 추가
                kakaoUser = kakaoUser.kakaoIdUpdate(kakaoId);
            } else {
                // 신규 회원가입
                // password: random UUID
                String password = UUID.randomUUID().toString();
                String encodedPassword = passwordEncoder.encode(password);

                // email: kakao email
                String email = kakaoUserInfo.getEmail();
                String nickname;

                Optional<Users> user = userRepository.findByNickName(kakaoUserInfo.getNickname());

                if (user.isPresent()) {
                    Random random = new Random();
                    int randomNumber = random.nextInt(100);
                    nickname = kakaoUserInfo.getNickname() + randomNumber;
                } else {
                    nickname = kakaoUserInfo.getNickname();
                }
                kakaoUser = new Users(email, password, nickname, kakaoId);
            }
            userRepository.save(kakaoUser);
            log.info(" " + kakaoUser);
        }
        return kakaoUser;
    }
}