package com.sparta.team2project.notify.service;

import com.sparta.team2project.comments.entity.Comments;
import com.sparta.team2project.comments.repository.CommentsRepository;
import com.sparta.team2project.commons.dto.MessageResponseDto;
import com.sparta.team2project.commons.exceptionhandler.CustomException;
import com.sparta.team2project.commons.exceptionhandler.ErrorCode;
import com.sparta.team2project.notify.Dto.NotifyResponseDto;
import com.sparta.team2project.notify.controller.NotifyController;
import com.sparta.team2project.notify.entity.Notify;
import com.sparta.team2project.notify.repository.NorifyRepository;
import com.sparta.team2project.posts.entity.Posts;
import com.sparta.team2project.posts.repository.PostsRepository;
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

    private final NorifyRepository norifyRepository;
    private final PostsRepository postsRepository;
    private final CommentsRepository commentsRepository;

    private static Map<Long, Integer> notifyCounts = new HashMap<>();


    // 알림 구독 기능 수행
    // Spring 에서 제공하는 SseEmitter 를 생성 후 저장한 다음
    // 필요할 때마다 구독자가 생성한 SseEmitter를 불러와서 이벤트에 대한 응답 전송
    // Controller 에서 가져온 수신자의 식별정보와 마지막 이벤트 식별자를 받음
    public SseEmitter subscribe (Long usersId) {

        // 현재 클라이언트를 위한 sseEmitter 생성
        SseEmitter sseEmitter = new SseEmitter(Long.MAX_VALUE);
        try {
            // 연결
            sseEmitter.send(SseEmitter.event().name("connect"));
        } catch (IOException e) {
           e.getStackTrace();
        }

        // user 의 pk 값을 key 로 해서 sseEmitter 를 저장
        NotifyController.sseEmitters.put(usersId, sseEmitter);

        // 완료, 타임아웃, 에러 발생시 SseEmitter를 emitterRepository 에서 삭제하도록 설정
        sseEmitter.onCompletion(() -> NotifyController.sseEmitters.remove(usersId));
        sseEmitter.onTimeout(() -> NotifyController.sseEmitters.remove(usersId));
        sseEmitter.onError((e) -> NotifyController.sseEmitters.remove(usersId));

        // 생성된 SseEmitter를 반환하여 클라이언트에게 전달
        // 클라이언트는 이를 통해 서버로부터 알림 이벤트를 수신 / 처리 가능
        return sseEmitter;
    }

    // 댓글 알림 - 게시글 작성자에게
    public void notifyComments (Long postId) {
        Posts posts = postsRepository.findById(postId).orElseThrow(
                ()-> new CustomException(ErrorCode.POST_NOT_EXIST)); // 존재하지 않는 게시글 입니다

        Comments comments = commentsRepository.findFirstByPosts_IdOrderByCreatedAtDesc(postId).orElseThrow(
                ()->new CustomException(ErrorCode.COMMENTS_NOT_EXIST)); //존재하지 않는 댓글입니다

        Long userId = posts.getUsers().getId();

        if (NotifyController.sseEmitters.containsKey(userId)) {
            SseEmitter sseEmitters = NotifyController.sseEmitters.get(userId);
            try {
                Map<String, String> eventData = new HashMap<>();
                eventData.put("message", "댓글이 달렸습니다");
                eventData.put("sender", comments.getNickname());
                eventData.put("contents", comments.getContents());

                // DB 저장
                Notify notify = new Notify(comments.getNickname(), comments.getContents(), posts);
                norifyRepository.save(notify);

                // 알림 개수 증가
                notifyCounts.put(userId, notifyCounts.getOrDefault(userId, 0) + 1);

                // 현재 알림 개수 전송
                sseEmitters.send(SseEmitter.event().name("notifyCounts").data(notifyCounts.get(userId)));

            } catch (IOException e) {
                NotifyController.sseEmitters.remove(userId);
            }
        }
    }

    // 조회
    public List<NotifyResponseDto> notifyList(Users users) {
        List<Notify> notifyList = norifyRepository.findAllByAndEmailOrderByCreatedAtDesc(users);
        List<NotifyResponseDto> notifyResponseDto = notifyList.stream()
                .map(NotifyResponseDto::new)
                .collect(Collectors.toList());
        return notifyResponseDto;
    }

    // 알림 삭제
    public MessageResponseDto notifyDelete(Long notifyId) throws IOException {

        Notify notify = norifyRepository.findById(notifyId).orElseThrow(
                () -> new CustomException(ErrorCode.NOTIFY_NOT_EXIST));

        Long userId = notify.getPosts().getUsers().getId();

        norifyRepository.delete(notify);

        // 알림 개수 감소
        if (notifyCounts.containsKey(userId)) {
            int currentCount = notifyCounts.get(userId);
            if (currentCount > 0) {
                notifyCounts.put(userId, currentCount - 1);
            }
        }

        // 현재 알림 개수 전송
        SseEmitter sseEmitter = NotifyController.sseEmitters.get(userId);
        sseEmitter.send(SseEmitter.event().name("notifyCounts").data(notifyCounts.get(userId)));

        return new MessageResponseDto("알림이 삭제 되었습니다", 200);
    }
}
