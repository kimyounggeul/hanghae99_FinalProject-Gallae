package com.sparta.team2project.replies.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor // test 에 사용
@NoArgsConstructor // test 에 사용
public class RepliesRequestDto {
    private String contents;
}

