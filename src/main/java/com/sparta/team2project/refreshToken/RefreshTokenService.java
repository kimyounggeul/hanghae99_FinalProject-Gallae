//package com.sparta.team2project.refreshToken;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//
//import java.security.SecureRandom;
//import java.util.Base64;
//import java.util.Date;
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//public class RefreshTokenService {
//    private final RefreshTokenRepository refreshTokenRepository;
//
////    public RefreshToken createRefreshToken(String email) {
////        // 새 리프레시 토큰 생성 및 DB에 저장
////        String tokenValue = generateNewTokenValue();
////        RefreshToken refreshToken = new RefreshToken(tokenValue, email);
////        refreshTokenRepository.save(refreshToken);
////        return refreshToken;
////    }
////
////    @Scheduled(fixedRate = 3600000) // 1시간마다 실행 (3600000 밀리초)
////    public void deleteExpiredTokens() {
////        // 현재 시간
////        Date currentTime = new Date();
////
////        // 만료된 리프레시 토큰 조회
////        List<RefreshToken> expiredTokens = refreshTokenRepository.findExpiredTokens(currentTime);
////
////        // 만료된 리프레시 토큰 삭제
////        for (RefreshToken token : expiredTokens) {
////            refreshTokenRepository.delete(token);
////        }
////    }
////    private String generateNewTokenValue() {
////        byte[] randomBytes = new byte[64]; // 원하는 토큰 길이에 맞게 조정
////        SecureRandom secureRandom = new SecureRandom();
////        secureRandom.nextBytes(randomBytes);
////        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
////    }
//}
