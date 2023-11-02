package com.sparta.team2project.posts.dto;

import com.sparta.team2project.posts.entity.PostCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class UpdateRequestDto {
    private String title;
    private String contents;
    private String subTitle;
    private PostCategory postCategory;
    private List<String> tagsList;
}
