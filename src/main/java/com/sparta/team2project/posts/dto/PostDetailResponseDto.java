package com.sparta.team2project.posts.dto;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class PostDetailResponseDto {
    private final String comments;
    private final String nickName;
    private final LocalDateTime createdAt;
    private final LocalDateTime modifiedAt;
    private final List<ReplyResponseDto> replyList;

    public PostDetailResponseDto(String comments, String nickName,LocalDateTime createdAt,LocalDateTime modifiedAt, List<ReplyResponseDto> replyList) {
        this.comments = comments;
        this.nickName = nickName;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
        this.replyList = replyList;
    }
}
