package com.sparta.team2project.profile.dto;

import com.sparta.team2project.commons.dto.MessageResponseDto;
import lombok.Getter;

@Getter
public class ProfileImgResponseDto {
    private MessageResponseDto messageResponseDto;
    private String picturesName;
    private String picturesURL;
    private String pictureContentType;
    private Long pictureSize;

    public ProfileImgResponseDto(MessageResponseDto messageResponseDto,
                                 String picturesName,
                                 String picturesURL,
                                 String pictureContentType,
                                 Long pictureSize) {
        this.messageResponseDto = messageResponseDto;
        this.picturesName = picturesName;
        this.picturesURL = picturesURL;
        this.pictureContentType = pictureContentType;
        this.pictureSize = pictureSize;
    }
}
