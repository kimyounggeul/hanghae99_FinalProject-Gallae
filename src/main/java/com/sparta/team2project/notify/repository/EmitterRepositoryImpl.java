package com.sparta.team2project.notify.repository;

import lombok.NoArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


@Repository
@NoArgsConstructor
public class EmitterRepositoryImpl implements EmitterRepository{
    // ConcurrentHashMap 동시에 여러 스레드가 접근하더라도 안전하게 데이터를 조작할 수 있도록 보장
    // 동시성 문제를 해결하고 맵에 데이터 저장 / 조회가능
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    private final Map<String, Object> eventCache = new ConcurrentHashMap<>();

    // Emitter 저장
    @Override
    public SseEmitter save (String emitterId, SseEmitter sseEmitter) {
        emitters.put(emitterId, sseEmitter);
        return sseEmitter;
    }

    // Event 저장
    @Override
    public void saveEventCache(String eventCacheId, Object event) {
        eventCache.put(eventCacheId, event);
    }

    // 구분자로 회원 ID를 사용하기에 StartWith를 사용 - 해당 회원과 관련된 모든 Emitter를 찾는다
    @Override
    public Map<String, SseEmitter> findAllEmitterStartWithByUsersId (String usersId) {
        return emitters.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(usersId))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    //  // 해당 회원과 관련된 모든 이벤트를 찾는다
    @Override
    public Map<String, Object> findAllEventCacheStartWithByUsersId (String usersId) {
        return emitters.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(usersId))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    // Emitter 삭제
    @Override
    public void deleteById (String id) {
        emitters.remove(id);
    }

    // 해당 회원과 관련된 모든 Emiitter를 지운다
    @Override
    public void deleteAllEmitterStartWithId (String usersId) {
        emitters.forEach(
                (key, emitter) -> {
                    if (key.startsWith(usersId)) {
                        emitters.remove(key);
                    }
                }
        );
    }

    // // 해당 회원과 관련된 모든 event를 지운다
    @Override
    public void deleteAllEventCacheStartWithId (String usersId) {
        eventCache.forEach(
                (key, emitter) ->{
                    if (key.startsWith(usersId)) {
                        eventCache.remove(key);
                    }
                }
        );
    }
}