package com.sparta.team2project.posts.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@Getter
public class LikeResponseDto {
    private String msg;
    private int statusCode;
    private boolean check;

    public LikeResponseDto(String msg, int statusCode,boolean check){
        this.msg = msg;
        this.statusCode = statusCode;
        this.check=check;
    }
}
