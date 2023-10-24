package com.sparta.team2project.replies.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sparta.team2project.replies.entity.Replies;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RepliesResponseDto {
    private Long repliesId;
    private String contents;
    private String email;
    private String checkUser;
    private LocalDateTime createAt;
    private LocalDateTime modifiedAt;

    public RepliesResponseDto(Replies replies, String users) {
        this.repliesId = replies.getId();
        this.contents = replies.getContents();
        this.createAt = replies.getCreatedAt();
        this.modifiedAt = replies.getModifiedAt();
        this.email = users;
    }

    public RepliesResponseDto(Replies replies, String users, String checkUser) {
        this.repliesId = replies.getId();
        this.contents = replies.getContents();
        this.createAt = replies.getCreatedAt();
        this.modifiedAt = replies.getModifiedAt();
        this.email = users;
        this.checkUser = checkUser;
    }
}

