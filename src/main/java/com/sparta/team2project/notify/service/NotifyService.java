package com.sparta.team2project.notify.service;

import com.sparta.team2project.notify.Dto.NotifyResponseDto;
import com.sparta.team2project.notify.entity.Notify;
import com.sparta.team2project.notify.entity.NotifyType;
import com.sparta.team2project.notify.repository.EmitterRepository;
import com.sparta.team2project.notify.repository.NorifyRepository;
import com.sparta.team2project.users.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;

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
    private  static  final Long DEFAULT_TIMEOUT = 60L * 1000 * 60;

    private final EmitterRepository emitterRepository;
    private final NorifyRepository norifyRepository;

    // 알림 구독 기능 수행
    // Spring 에서 제공하는 SseEmitter 를 생성 후 저장한 다음
    // 필요할 때마다 구독자가 생성한 SseEmitter를 불러와서 이벤트에 대한 응답 전송
    // Controller 에서 가져온 수신자의 식별정보와 마지막 이벤트 식별자를 받음
    public SseEmitter subscribe (String nickName, String lastEventid) {

        // emitterID 생성 nickName 을 포함하여 SseEmitter 를 식별하기 위한 고유 아이디생성
        String emitterId = makeTimeIncludeId(nickName);

        // 새로운 SseEmitter 객체를 생성하고 emitterId 를 키로 사용해 emitterRepository 에 저장
        // 이렇게 생성된 SseEmitter 는 클라이언트에게 이벤트를 전송하는 역활 수행
        SseEmitter emitter = emitterRepository.save(emitterId, new SseEmitter((DEFAULT_TIMEOUT)));

        // 완료, 타임아웃, 에러 발생시 SseEmitter를 emitterRepository 에서 삭제하도록 설정
        emitter.onCompletion(() -> emitterRepository.deleteById(emitterId));
        emitter.onTimeout(() -> emitterRepository.deleteById(emitterId));
        emitter.onError((e) -> emitterRepository.deleteAllEmitterStartWithId(emitterId));

        // 503 에러를 방지하기 위한 더미 이벤트 전송
        String eventId = makeTimeIncludeId(nickName);
        sendNotify(emitter, eventId, emitterId, "EventStream Created. [userEmail=" + nickName + "]");

        // 미수신한 이벤트 전송
        // 만약 클라이언트가 마지막으로 수신한 이벤트 ID 인 lastEventId 가 존재하면
        // 이전에 발생한 이벤트 중 해당이벤트 이후의 이벤트들을 캐시에서 가져와 클라이언트에게 전송
        // 이를 통해 클라이언트가 놓친 이벤트를 보상하여 데이터 유실 예방
        if (hasLostData(lastEventid)) {
            sendLostData(lastEventid, nickName, emitterId, emitter);
        }

        // 생성된 SseEmitter를 반환하여 클라이언트에게 전달
        // 클라이언트는 이를 통해 서버로부터 알림 이벤트를 수신 / 처리 가능
        return emitter;
    }

    // EmitterId, eventId를 생성
    // 시간이 있는 이유 : ID 값만 사용하면 데이터가 언제 보내졌는지 유실되었는지 알 수없다
    //                 따라서 System.currentTimeMillis() 를 붙여두면 데이터가 유실된 시점을 하악할 수 있음으로
    //                 저장된 key 값 비교를 통해 유실된 데이터만 재전송 가능
    private String makeTimeIncludeId (String email) {
        return email + "_" + System.currentTimeMillis();
    }

    // SsEmitter 객체를 사용해서 SSE 를 클라이언트에게 전송하는 역활
    private void sendNotify (SseEmitter emitter, String eventId, String emitterId, Object data) {
        try {
            // 더미데이터 전송
            emitter.send(SseEmitter.event()
                    .id(eventId) // 이벤트의 고유식별자 설정
                    .name("see") // 이벤트의 이름 설정
                    .data(data) // 이벤트로 전송할 데이터 설정
            );
            // 만약 클라이언트의 연결이 끊어져 해당 예외를 캐치하면 끊긴 SseEmitter 객체를 제거하여 정리
        } catch (IOException exception) {
            emitterRepository.deleteById(emitterId);
        }
    }

    // lastEventId 가 비어있지 않은지 확인하여 클라이언트가 이전 이벤트 이후에 새로운 이벤트를 놓치지 않았는지 확인
    private boolean hasLostData (String lastEventId) {

        // lastEvnetId 가 비어있지 않을 때 -> Controller 의 헤더를 통해 lastEvnetId 가 들어옴 -> 손실된 이벤트가 있다 ->  true 리턴
        // 즉, 클라이언트가 이전 이벤트 이후에 새로운 이벤트를 받지 않았으므로 이후 발생한 이벤트들이 손실됨을 의미
        // lastEvnetId 비어있을때 -> Controller 의 헤더를 통해 lastEvnetId 가 들어오지않음 -> 손실된 이벤트가 없다 ->  false 리턴
        // 즉, 클라이언트가 이전에 받은 이벤트 이후에 새로운 이벤트들을 놓치지 않았다는 의미
        return !lastEventId.isEmpty();
    }

    // 수신자에게 전송되지 못한 이벤트 데이터를 캐시에서 가져와 클라이언트에게 전송하는 과정 수행
    private void sendLostData (String lastEventId, String userEmail, String emitterId, SseEmitter emitter) {

        // 수신자의 이메일을 기준으로 캐시된 이벤트 데이터를 가져옴
        Map<String, Object> eventCaches = emitterRepository.findAllEventCacheStartWithByUsersId(String.valueOf(userEmail));
        // eventCaches 맵 객체의 엔트리들을 스트림으로 변환해 순환 entry 는 키와 값의 쌍으로 구성됨
        eventCaches.entrySet().stream()
                // lastEventId 가 entry 의 키보다 작을 때만 필터링
                // lastEventId.compareTo(entry.getKey())는 lastEventId와 entry.getKey()를 비교하여 순서를 나타내는 정수 값을 반환하는데,
                // 무엇이 더 크냐에 따라 정수값의 부호가 달라진다.
                // 음수 값일 경우 : lastEventId < entry.getKey()
                // 0일 경우 : lastEventId == entry.getKey()
                // 양수 값일 경우 : lastEventId > entry.getKey()
                //⠀여기서는 lastEventId가 엔트리 키보다 작아야 하는 경우이기 때문에 음수값을 필터링하기 위해 < 0 이 있어야 함
                .filter(entry -> lastEventId.compareTo(entry.getKey()) < 0)
                // 필터리된 각 entry 에 대해 sendNotify() 를 호출하여 SseEmitter 를 통해 해당 알림을 클라이언트에게 전송
                .forEach(entry -> sendNotify(emitter, entry.getKey(), emitterId, entry.getValue()));
    }

    // 알림을 생성하고 지정된 수신자에게 알림을 전송하는 기능 수행
    // 알림 수신자, 알림 유형, 내용, URL 등의 정보를 인자로 받음
    // 알림을 수신하는 모든 수신자에게 알림을 전송하기 위해 emitterRepository 에서 해당 수신자의
    // 모든 SseEmitter 를 가져와 알림을 전송하고, 동시에 emitterRepository 에 이벤트 캐시 저장
    public void send (Users users, NotifyType notifyType, String content, String url) {

        // 알림 객체 생성 및 저장 (수신자, 알림 유형, 내용, url 저장)
        Notify notify = norifyRepository.save(createNotify(users, notifyType, content, url));

        // 수시자의 Email 을 usersEmail 에 저장 SseEmitter 객체에서 관리되는 emitterRepository 에서 사용됨
        String usersEmail = users.getEmail();

        // 이벤트 ID 생성 SseMitter 로 전송되는 이벤트의 고유 식별자로 사용됨
        String eventId = usersEmail + "_" + System.currentTimeMillis();

        // 수신자에 연결된 모든 SseEmitter 객체를 emitters 변수에 가져옴
        // 수신자가 여러 클라이언트와 연결된 경우를 대비해 다중 연결을 지원하기 위한 작업
        Map<String, SseEmitter> emitters = emitterRepository.findAllEmitterStartWithByUsersId(usersEmail);

        // emitters 를 순환하며 각 SseEmitter 객체에 알림 전송
        emitters.forEach(
                // forEach 메서드는 맵의 각 항목을 가져와서 해당 키를 key 변수에, 해당값을 emitter 변수에 자동할당
                (key, emitter) -> {
                    emitterRepository.saveEventCache(key, notify);
                    // sendNotify() 메서드를 호출하여 알림을 SseEmitter 객체로 전송
                    sendNotify(emitter, eventId, key, new NotifyResponseDto(notify));
                }
        );
    }

    // 파라미터로 받은 값들로 알림 객체를 bulder 를 이용해 생성
    private Notify createNotify (Users users, NotifyType notifyType, String content, String url) {
        return Notify.builder()
                .users(users)
                .notifyType(notifyType)
                .content(content)
                .url(url)
                .isRead(false)
                .build();
    }
}
