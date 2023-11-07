package com.sparta.team2project.notify.Dto;

import com.sparta.team2project.notify.entity.Notify;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class NotifyResponseDto {
    private Long id;
    private String content;
    private LocalDateTime createAt;


    public NotifyResponseDto(Notify notify) {
        this.id = notify.getId();
        this.content = notify.getContents();
        this.createAt = notify.getCreatedAt();
    }
}
