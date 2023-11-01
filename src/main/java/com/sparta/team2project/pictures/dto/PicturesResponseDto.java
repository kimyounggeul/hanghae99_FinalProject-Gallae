package com.sparta.team2project.pictures.dto;

import com.sparta.team2project.pictures.entity.Pictures;
import lombok.Getter;

@Getter
public class PicturesResponseDto {
    private final Long schedulesId;
    private Long picturesId;
    private final String picturesURL;
    private final String picturesName;
    private final String pictureContentType;
    private final Long pictureSize;

    public PicturesResponseDto(Long schedulesId,
                               String picturesURL,
                               String picturesName,
                               String pictureContentType,
                               Long pictureSize
                               ) {
        this.schedulesId = schedulesId;
        this.picturesURL = picturesURL;
        this.picturesName = picturesName;
        this.pictureContentType = pictureContentType;
        this.pictureSize = pictureSize;
    }

    public PicturesResponseDto(Pictures pictures){
        this.schedulesId = pictures.getSchedules().getId();
        this.picturesId = pictures.getId();
        this.picturesURL = pictures.getPicturesURL();
        this.picturesName = pictures.getPicturesName();
        this.pictureContentType = pictures.getPictureContentType();
        this.pictureSize = pictures.getPictureSize();
    }

}
