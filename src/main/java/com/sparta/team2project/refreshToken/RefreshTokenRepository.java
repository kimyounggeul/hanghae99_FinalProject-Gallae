package com.sparta.team2project.refreshToken;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository <RefreshToken, Long>{
    boolean existsByRefreshToken(String refreshToken) ;
    Optional<RefreshToken> findByRefreshToken(String refreshToken);
}
