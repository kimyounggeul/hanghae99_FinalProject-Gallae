package com.sparta.team2project.posts.dto;

import com.sparta.team2project.commons.dto.MessageResponseDto;
import com.sparta.team2project.posts.dto.PostsPicturesResponseDto;
import lombok.Getter;

@Getter
public class PostsPicturesMessageResponseDto {
    private final MessageResponseDto messageResponseDto;
    private final PostsPicturesResponseDto postsPicturesResponseDto;

    public PostsPicturesMessageResponseDto(PostsPicturesResponseDto postsPicturesResponseDto, MessageResponseDto messageResponseDto) {
        this.messageResponseDto = messageResponseDto;
        this.postsPicturesResponseDto = postsPicturesResponseDto;
    }
}