package com.sparta.team2project.comments.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sparta.team2project.comments.entity.Comments;
import com.sparta.team2project.replies.dto.RepliesResponseDto;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommentsResponseDto {
    private Long commentId;
    private String contents;
    private String email;
    private String nickname;
    private String checkUser;
    private LocalDateTime createAt;
    private LocalDateTime modifiedAt;
    private List<RepliesResponseDto> repliesList = new ArrayList<>();



    public CommentsResponseDto(Comments comments, List<RepliesResponseDto> repliesList) {
        this.commentId = comments.getId();
        this.contents = comments.getContents();
        this.createAt = comments.getCreatedAt();
        this.modifiedAt = comments.getModifiedAt();
        this.email = comments.getEmail();
        this.nickname = comments.getNickname();
        this.repliesList = repliesList;
    }

    public CommentsResponseDto(Comments comments, String checkUser, List<RepliesResponseDto> repliesList) {
        this.commentId = comments.getId();
        this.contents = comments.getContents();
        this.createAt = comments.getCreatedAt();
        this.modifiedAt = comments.getModifiedAt();
        this.email = comments.getEmail();
        this.nickname = comments.getNickname();
        this.checkUser = checkUser;
        this.repliesList = repliesList;
    }
}

