package com.sparta.team2project.notify.Dto;

import com.sparta.team2project.notify.entity.Notify;
import lombok.Getter;
import org.springframework.security.core.userdetails.User;

import java.util.List;

@Getter
public class NotifyResponseDto {
    private Long notifyId;
    private UserDto receiver;
    private UserDto sender;
    private String message;
    private boolean isRead;

 public NotifyResponseDto(Notify notify) {
     this.notifyId = notify.getId();
     this.receiver = new UserDto((notify.getReceiver()));
     this.sender = new UserDto(notify.getSender());
     this.message = notify.getMessage();
     this.isRead = notify.getIsRead();
 }
}

