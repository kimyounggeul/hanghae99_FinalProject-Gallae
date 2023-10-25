package com.sparta.team2project.posts.dto;

import com.sparta.team2project.commons.dto.MessageResponseDto;
import com.sparta.team2project.pictures.dto.PicturesResponseDto;
import com.sparta.team2project.posts.dto.PostsPicturesResponseDto;
import lombok.Getter;

import java.util.List;

@Getter
public class PostsPicturesUploadResponseDto {

    private final MessageResponseDto messageResponseDto;
    private final List<PostsPicturesResponseDto> postsPicturesResponseDtoList;

    public PostsPicturesUploadResponseDto(List<PostsPicturesResponseDto> postsPicturesResponseDtoList, MessageResponseDto messageResponseDto) {
        this.postsPicturesResponseDtoList = postsPicturesResponseDtoList;
        this.messageResponseDto = messageResponseDto;
    }
}
