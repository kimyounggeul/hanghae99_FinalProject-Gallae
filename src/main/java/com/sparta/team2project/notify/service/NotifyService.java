package com.sparta.team2project.notify.service;

import com.sparta.team2project.commons.dto.MessageResponseDto;
import com.sparta.team2project.commons.entity.UserRoleEnum;
import com.sparta.team2project.commons.exceptionhandler.CustomException;
import com.sparta.team2project.commons.exceptionhandler.ErrorCode;
import com.sparta.team2project.notify.Dto.NotifyResponseDto;
import com.sparta.team2project.notify.entity.Notify;
import com.sparta.team2project.notify.repository.EmitterRepository;
import com.sparta.team2project.notify.repository.NotifyRepository;
import com.sparta.team2project.users.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotifyService {

    // emitterId : SseEmitter 를 구분 / 관리하기 위한 식별자
    //             emitterRepository 에 저장되어 특정 클라이언트의 연결을 관리하는데 사용됨
    // eventId : 개별 알림 이벤트를 식별하기 위한 고유 값
    //           각 알림 이벤트는 고유한 eventId 를 가지고 있고 클라이언트에게 전송될 때 이벤트의 식별을 위해 사용됨
    // subscribe() : 클라이언트와의 SSE 스트림 통신을 유지하면서 연결을 생성하고 유지
    // send() : 알림을 생성하고 해당알림을 수신하는 모든 클라이언트에게 전송

    // SSE 연결 지속시간 설정
    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60;

    private final EmitterRepository emitterRepository;
    private final NotifyRepository notifyRepository;

    private static Map<Long, Integer> notifyCounts = new HashMap<>();

    // 알림 구독 기능 수행
    // Spring 에서 제공하는 SseEmitter 를 생성 후 저장한 다음
    // 필요할 때마다 구독자가 생성한 SseEmitter를 불러와서 이벤트에 대한 응답 전송
    // Controller 에서 가져온 수신자의 식별정보와 마지막 이벤트 식별자를 받음
    public SseEmitter subscribe(Users users, String lastEventId) {

        Long userId = users.getId();
        // emitterID 생성 nickName 을 포함하여 SseEmitter 를 식별하기 위한 고유 아이디생성
        String emitterId = userId + "_" + System.currentTimeMillis();

        // 새로운 SseEmitter 객체를 생성하고 emitterId 를 키로 사용해 emitterRepository 에 저장
        // 이렇게 생성된 SseEmitter 는 클라이언트에게 이벤트를 전송하는 역활 수행
        SseEmitter emitter = emitterRepository.save(emitterId, new SseEmitter((DEFAULT_TIMEOUT)));

        // 완료, 타임아웃, 에러 발생시 SseEmitter를 emitterRepository 에서 삭제하도록 설정
        emitter.onCompletion(() -> emitterRepository.deleteById(emitterId));
        emitter.onTimeout(() -> emitterRepository.deleteById(emitterId));
        emitter.onError((e) -> emitterRepository.deleteAllEmitterStartWithId(emitterId));

        // 503 에러를 방지하기 위한 더미 이벤트 전송
        sendNotify(emitter, emitterId, "EventStream Created. [userId=" + userId + "]");

        if (lastEventId != null && !lastEventId.isEmpty()) {
            Map<String, Object> events = emitterRepository.findAllEventCacheStartWithByUsersId(String.valueOf(userId));
            events.entrySet().stream()
                    .filter(entry -> lastEventId.compareTo(entry.getKey()) < 0)
                    .forEach(entry -> sendNotify(emitter, entry.getKey(), entry.getValue()));
        }
        // 생성된 SseEmitter를 반환하여 클라이언트에게 전달
        // 클라이언트는 이를 통해 서버로부터 알림 이벤트를 수신 / 처리 가능
        return emitter;
    }

    // SsEmitter 객체를 사용해서 SSE 를 클라이언트에게 전송하는 역활
    private void sendNotify(SseEmitter emitter, String eventId,Object data) {
        try {
            // 더미데이터 전송
            emitter.send(SseEmitter.event()
                    .id(eventId) // 이벤트의 고유식별자 설정
                    .name("see") // 이벤트의 이름 설정
                    .data(data) // 이벤트로 전송할 데이터 설정
            );
            // 만약 클라이언트의 연결이 끊어져 해당 예외를 캐치하면 끊긴 SseEmitter 객체를 제거하여 정리
        } catch (IOException exception) {
            emitterRepository.deleteById(eventId);
            throw new CustomException(ErrorCode.RUN_TIME_ERROR); // 연결에 실패했습니다
        }
    }

    public void send(Users receiver, Users sender, String message) {
        Notify notify = createNotify(receiver, sender, message);
        // 이벤트 ID 생성 (SseEmitter로 전송되는 이벤트의 고유 식별자로 사용됨)
        String eventId = String.valueOf(receiver.getId());
        // 알림 객체 생성 및 저장 (수신자, 내용 저장)
        notifyRepository.save(notify);

        // 이 메서드는 해당 수신자에 연결된 모든 SseEmitter 객체를 가져와 알림을 전송합니다
        // 알림을 수신하는 모든 수신자에게 알림을 전송하고 동시에 emitterRepository에 이벤트 캐시를 저장합니다
        Map<String, SseEmitter> emitters = emitterRepository.findAllEmitterStartWithByUsersId(eventId);

        emitters.forEach((key, emitter) -> {
            // 데이터 캐시 저장 (유실된 데이터 처리를 위함)
            emitterRepository.saveEventCache(key, notify);
            // 데이터 전송
            sendNotify(emitter, key, new NotifyResponseDto(notify));
        });
    }

    // 파라미터로 받은 값들로 알림 객체를 bulder 를 이용해 생성
    private Notify createNotify(Users receiver, Users sender, String message) {
        return Notify.builder()
                .sender(sender)
                .receiver(receiver)
                .message(message)
                .isRead(false)
                .build();
    }

    // 조회
    public List<NotifyResponseDto> notifyList(Users users) {
        List<Notify> notify = notifyRepository.findByReceiver(users);
        List<NotifyResponseDto> notifyResponseDto = notify.stream()
                .map(NotifyResponseDto::new)
                .collect(Collectors.toList());
        return notifyResponseDto;
    }

    // 알림 삭제
    public MessageResponseDto notifyDelete(Long notifyId, Users users) {

        Notify notify = notifyRepository.findById(notifyId).orElseThrow(
                () -> new CustomException(ErrorCode.NOTIFY_NOT_EXIST)); // 존재하지 않는 알림입니다

        if (users.getUserRole() == UserRoleEnum.ADMIN) {
            notifyRepository.delete(notify);
            return new MessageResponseDto("관리자가 알림을 삭제하였습니다", 200);
        } else if (notify.getReceiver().getId().equals(users.getId())) {
            notifyRepository.delete(notify);
            return new MessageResponseDto("알림을 삭제하였습니다", 200);
        } else {
            throw new CustomException(ErrorCode.NOT_ALLOWED); // 권한이 없습니다
        }
    }
}

