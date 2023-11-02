package com.sparta.team2project.posts.dto;

import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class ReplyResponseDto {
    private final String contents;
    private final String nickName;
    private final LocalDateTime createdAt;
    private final LocalDateTime modifiedAt;

    public ReplyResponseDto(String contents, String nickName, LocalDateTime createdAt, LocalDateTime modifiedAt) {
        this.contents = contents;
        this.nickName = nickName;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
    }
}
