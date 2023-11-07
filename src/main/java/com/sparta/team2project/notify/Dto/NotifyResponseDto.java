package com.sparta.team2project.notify.Dto;

import com.sparta.team2project.notify.entity.Notify;
import com.sparta.team2project.notify.entity.NotifyType;

public class NotifyResponseDto {
    private Long id;
    private String content;
    private String url;
    private NotifyType notifyType;
    private Boolean isRead;

    public NotifyResponseDto(Notify notify) {
        this.id = notify.getId();
        this.content = notify.getContent();
        this.url = String.valueOf(url);
        this.notifyType = notify.getNotifyType();
        this.isRead = notify.getIsRead();
    }
}
