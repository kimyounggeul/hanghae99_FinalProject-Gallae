package com.sparta.team2project.refreshToken;

import com.sparta.team2project.commons.Util.JwtUtil;
import com.sparta.team2project.commons.Util.RedisUtil;
import com.sparta.team2project.commons.entity.UserRoleEnum;
import com.sparta.team2project.commons.exceptionhandler.CustomException;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/token")
public class TokenController {
    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil; // RedisUtil 클래스 주입


    @PostMapping("/refreshAccessToken")
    public ResponseEntity<TokenDto> refreshAccessToken(@RequestHeader(JwtUtil.REFRESH_KEY) String refreshToken) {
        try {
            // 리프레시 토큰 검증
            if (jwtUtil.validateToken(refreshToken)) {
                // 리프레시 토큰에서 사용자 정보를 가져와 새로운 액세스 토큰 생성
                Claims claims = jwtUtil.getUserInfoFromToken(refreshToken);
                String email = claims.getSubject();
                UserRoleEnum userRole = UserRoleEnum.valueOf(claims.get("userRole").toString());
                if (claims.containsKey("email")) {
                    userRole = UserRoleEnum.valueOf(claims.get("userRole").toString());
                }
                // 새로운 액세스 토큰 생성
                String newAccessToken = jwtUtil.createAccessToken(email, userRole);

                // 새로운 액세스 토큰 반환
                return new ResponseEntity<>(new TokenDto(newAccessToken, refreshToken), HttpStatus.OK);
            } else {
                // 리프레시 토큰이 유효하지 않을 때의 처리
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } catch (CustomException e) {
            // 예외 처리
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }
}
