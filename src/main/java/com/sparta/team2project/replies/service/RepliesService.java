package com.sparta.team2project.replies.service;

import com.sparta.team2project.comments.entity.Comments;
import com.sparta.team2project.comments.repository.CommentsRepository;
import com.sparta.team2project.commons.dto.MessageResponseDto;
import com.sparta.team2project.commons.entity.UserRoleEnum;
import com.sparta.team2project.commons.exceptionhandler.CustomException;
import com.sparta.team2project.commons.exceptionhandler.ErrorCode;
import com.sparta.team2project.notify.service.NotifyService;
import com.sparta.team2project.replies.dto.RepliesMeResponseDto;
import com.sparta.team2project.replies.dto.RepliesRequestDto;
import com.sparta.team2project.replies.entity.Replies;
import com.sparta.team2project.replies.repository.RepliesRepository;
import com.sparta.team2project.users.Users;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor

public class RepliesService {
    private final RepliesRepository repliesRepository;
    private final CommentsRepository commentsRepository;
    private final NotifyService notifyService;

    // 대댓글 생성
    public MessageResponseDto repliesCreate(Long commentId,
                                            RepliesRequestDto requestDto,
                                            Users users) {

        Comments comments = commentsRepository.findById(commentId).orElseThrow(
                () -> new CustomException(ErrorCode.COMMENTS_NOT_EXIST)); // 존재하지 않는 댓글입니다

        Replies replies = new Replies(requestDto, users, comments);
        repliesRepository.save(replies);

        // 댓글이 자신의 게시물에 작성된 것인지 확인
        if (!comments.getPosts().getUsers().getEmail().equals(replies.getEmail())) {
            notifyService.send(comments.getPosts().getUsers(), users, "새로운 대댓글이 있습니다");
        }

        return new MessageResponseDto ("대댓글을 작성하였습니다", 200);
    }

//    // 대댓글 조회
//    public Slice<RepliesResponseDto> repliesList(Long commentId,
//                                                 Pageable pageable) {
//
//        Comments comments = commentsRepository.findById(commentId).orElseThrow(
//                () -> new CustomException(ErrorCode.COMMENTS_NOT_EXIST)); // 존재하지 않는 댓글입니다
//
//        Posts posts = comments.getPosts();
//
//        Slice<Replies> repliesList = repliesRepository.findByComments_IdOrderByCreatedAtDesc(commentId, pageable);
//
//        if (repliesList.isEmpty()) {
//            throw new CustomException(ErrorCode.REPLIES_NOT_EXIST); // 존재하지 않는 대댓글입니다
//        }
//
//        List<RepliesResponseDto> RepliesResponseDtoList = new ArrayList<>();
//
//        for (Replies replies : repliesList) {
//            if (posts.getUsers().getEmail().equals(replies.getEmail())) {
//                RepliesResponseDtoList.add(new RepliesResponseDto(replies, "글쓴이"));
//            } else {
//                RepliesResponseDtoList.add(new RepliesResponseDto(replies));
//            }
//        }
//        return new SliceImpl<>(RepliesResponseDtoList, pageable, repliesList.hasNext());
//    }

    // 마이페이지에서 내가 쓴 대댓글 조회
    public Slice<RepliesMeResponseDto> repliesMeList(Users users,
                                                     Pageable pageable) {

        Slice<Replies> repliesMeList = repliesRepository.findAllByAndEmailOrderByCreatedAtDesc(users.getEmail(), pageable);

//        if (repliesMeList.isEmpty()) {
//            throw new CustomException(ErrorCode.REPLIES_NOT_EXIST); // 존재하지 않는 대댓글입니다
//        }

        List<RepliesMeResponseDto> RepliesMeResponseDtoList = new ArrayList<>();

        for (Replies replies : repliesMeList) {
            RepliesMeResponseDtoList.add(new RepliesMeResponseDto(replies, replies.getComments().getPosts().getTitle()));
        }

        return new SliceImpl<>(RepliesMeResponseDtoList, pageable, repliesMeList.hasNext());
    }

    // 대댓글 수정
    @Transactional
    public MessageResponseDto repliesUpdate( Long repliesId,
                                             RepliesRequestDto request,
                                             Users users) {

        Replies replies = findById(repliesId);
        checkAuthority(users,replies);
        replies.update(request, users);

        String message;
        if (users.getUserRole() == UserRoleEnum.ADMIN) {
            message = "관리자가 대댓글을 수정하였습니다" ;
        } else {
            message = "대댓글을 수정하였습니다";
        }
        return new MessageResponseDto(message, 200);
    }

    // 대댓글 삭제
    public MessageResponseDto repliesDelete(Long repliesId,
                                            Users users) {

        Replies replies = findById(repliesId);
        checkAuthority(users,replies);
        repliesRepository.delete(replies);

        String message;
        if (users.getUserRole() == UserRoleEnum.ADMIN) {
            message = "관리자가 대댓글을 삭제하였습니다" ;
        } else {
            message = "대댓글을 삭제하였습니다";
        }
        return new MessageResponseDto(message, 200);
    }


    private Replies findById(Long id) {
        return repliesRepository.findById(id).orElseThrow(
                () -> new CustomException(ErrorCode.REPLIES_NOT_EXIST)); // 존재하지 않는 대댓글입니다
    }

    public void checkAuthority (Users existUsers, Replies replies) {
        if (!existUsers.getUserRole().equals(UserRoleEnum.ADMIN) && !existUsers.getEmail().equals(replies.getEmail())){
            throw new CustomException(ErrorCode.NOT_ALLOWED); // 권한이 없습니다
        }
    }
}
