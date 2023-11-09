package com.sparta.team2project.notify.repository;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

public interface EmitterRepository {

    // Emitter 저장
    SseEmitter save (String emitterId, SseEmitter sseEmitter);

    // Event 저장
    void saveEventCache (String emitterId, Object event);

    // 해당 회원과 관련된 모든 Emitter를 찾는다
    Map<String, SseEmitter> findAllEmitterStartWithByUsersId(String usersId);

    // 해당 회원과 관련된 모든 이벤트를 찾는다
    Map<String, Object> findAllEventCacheStartWithByUsersId(String usersId);

    // Emitter 삭제
    void deleteById (String id);

    // 해당 회원과 관련된 모든 Emiitter를 지운다
    void deleteAllEmitterStartWithId (String usersId);

    // 해당 회원과 관련된 모든 event를 지운다
    void deleteAllEventCacheStartWithId (String usersId);
}