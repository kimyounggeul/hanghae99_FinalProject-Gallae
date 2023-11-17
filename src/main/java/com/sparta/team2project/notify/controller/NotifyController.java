package com.sparta.team2project.notify.controller;

import com.sparta.team2project.commons.dto.MessageResponseDto;
import com.sparta.team2project.commons.security.UserDetailsImpl;
import com.sparta.team2project.notify.Dto.NotifyResponseDto;
import com.sparta.team2project.notify.service.NotifyService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class NotifyController {
    private final NotifyService notifyService;

    // 알림 구독 기능 수행
    @Operation(summary = "알림 구독 기능 수행", description = "알림 구독 기능 수행 api 입니다.")
    @GetMapping(value ="/subscribe", produces = "text/event-stream")
    public SseEmitter subscribe (@AuthenticationPrincipal UserDetailsImpl userDetails,
                                 @RequestHeader(value = "Last-Event-Id", required = false, defaultValue = "") String lastEventid) {
        return notifyService.subscribe(userDetails.getUsers(), lastEventid);
    }

    // 전체 알림 조회
    @Operation(summary = "전체 알림 조회", description = "전체 알림 조회 api 입니다.")
    @GetMapping("/notify")
    public List<NotifyResponseDto> notifyList(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return notifyService.notifyList(userDetails.getUsers());
    }

    // 알림 삭제
    @Operation(summary = "알림 삭제", description = "알림 삭제 api 입니다.")
    @DeleteMapping("/notify/{notifyId}")
    public MessageResponseDto notifyDelete (@PathVariable("notifyId") Long notifyId,
                                            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        return notifyService.notifyDelete(notifyId, userDetails.getUsers());
    }
}
