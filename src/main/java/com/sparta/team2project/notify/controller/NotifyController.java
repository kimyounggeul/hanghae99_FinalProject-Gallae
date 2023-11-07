package com.sparta.team2project.notify.controller;

import com.sparta.team2project.commons.security.UserDetailsImpl;
import com.sparta.team2project.notify.service.NotifyService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notify")
public class NotifyController {
    private final NotifyService notifyService;

    // 알림 구독 기능 수행
    @Operation(summary = "알림 구독 기능 수행", description = "알림 구독 기능 수행 api 입니다.")
    @GetMapping(value ="/subscribe", produces = "text/event-stream")
    public SseEmitter subscribe (@AuthenticationPrincipal UserDetailsImpl userDetails,
                                      @RequestHeader(value = "Last-Event-Id", required = false, defaultValue = "") String lastEventid) {
        return notifyService.subscribe(userDetails.getUsername(), lastEventid);
    }
}
