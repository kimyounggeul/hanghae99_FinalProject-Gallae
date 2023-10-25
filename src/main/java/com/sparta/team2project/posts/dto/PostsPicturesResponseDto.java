package com.sparta.team2project.posts.dto;

import com.sparta.team2project.pictures.entity.Pictures;
import com.sparta.team2project.posts.entity.PostsPictures;
import lombok.Getter;

@Getter
public class PostsPicturesResponseDto {
    private final Long postId;
    private final String postsPicturesURL;
    private final String postsPicturesName;
    private final String postsPictureContentType;
    private final Long postsPictureSize;

    public PostsPicturesResponseDto(Long postId,
                                    String postsPicturesURL,
                                    String postsPicturesName,
                                    String postsPictureContentType,
                                    Long postsPictureSize
                               ) {
        this.postId = postId;
        this.postsPicturesURL = postsPicturesURL;
        this.postsPicturesName = postsPicturesName;
        this.postsPictureContentType = postsPictureContentType;
        this.postsPictureSize = postsPictureSize;
    }

    public PostsPicturesResponseDto(PostsPictures postsPictures){
        this.postId = postsPictures.getPosts().getId();
        this.postsPicturesURL = postsPictures.getPostsPicturesURL();
        this.postsPicturesName = postsPictures.getPostsPicturesName();
        this.postsPictureContentType = postsPictures.getPostsPictureContentType();
        this.postsPictureSize = postsPictures.getPostsPictureSize();
    }
}
