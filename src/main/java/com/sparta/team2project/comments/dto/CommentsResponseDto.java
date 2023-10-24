package com.sparta.team2project.comments.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sparta.team2project.comments.entity.Comments;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommentsResponseDto {
    private Long commentId;
    private String contents;
    private String email;
    private String checkUser;
    private LocalDateTime createAt;
    private LocalDateTime modifiedAt;



    public CommentsResponseDto(Comments comments, String users) {
        this.commentId = comments.getId();
        this.contents = comments.getContents();
        this.createAt = comments.getCreatedAt();
        this.modifiedAt = comments.getModifiedAt();
        this.email = users;
    }

    public CommentsResponseDto(Comments comments, String users, String checkUser) {
        this.commentId = comments.getId();
        this.contents = comments.getContents();
        this.createAt = comments.getCreatedAt();
        this.modifiedAt = comments.getModifiedAt();
        this.email = users;
        this.checkUser = checkUser;
    }
}
