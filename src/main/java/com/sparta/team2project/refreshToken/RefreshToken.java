package com.sparta.team2project.refreshToken;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column
    private String refreshToken;

    @NotNull
    @Column
    private String email;

    @Column
    private String kakaoAccessToken;

    @Column
    private String kakaoRefreshToken;

    public RefreshToken(String refreshToken, String email) {
        this.refreshToken = refreshToken;
        this.email = email;
    }

    public RefreshToken(String refreshToken, String email, String kakaoAccessToken, String kakaoRefreshToken) {
        this.refreshToken = refreshToken;
        this.email = email;
        this.kakaoAccessToken = kakaoAccessToken;
        this.kakaoRefreshToken = kakaoRefreshToken;
    }

    public RefreshToken updateToken(String refreshToken) {
        this.refreshToken = refreshToken;
        return this;
    }

    public RefreshToken updateToken(String refreshToken, String kakaoAccessToken, String kakaoRefreshToken) {
        this.refreshToken = refreshToken;
        this.kakaoAccessToken = kakaoAccessToken;
        this.kakaoRefreshToken = kakaoRefreshToken;
        return this;
    }
}
