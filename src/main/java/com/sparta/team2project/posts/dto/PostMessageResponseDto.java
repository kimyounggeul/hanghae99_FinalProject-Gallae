package com.sparta.team2project.posts.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.sparta.team2project.posts.entity.Posts;
import lombok.Getter;

import java.util.List;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostMessageResponseDto {
    private final Long postId;
    private final String msg;
    private final int statusCode;
    private List<Long> tripDateId;

    public PostMessageResponseDto(String msg, int statusCode, Posts posts, List<Long> tripDateId){
        this.postId= posts.getId();
        this.tripDateId = tripDateId;
        this.msg = msg;
        this.statusCode = statusCode;
    }
    public PostMessageResponseDto(String msg, int statusCode, Posts posts){
        this.postId= posts.getId();
        this.msg = msg;
        this.statusCode = statusCode;
    }
}


