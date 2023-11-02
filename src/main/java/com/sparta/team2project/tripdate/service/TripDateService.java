package com.sparta.team2project.tripdate.service;

import com.sparta.team2project.commons.dto.MessageResponseDto;
import com.sparta.team2project.commons.entity.UserRoleEnum;
import com.sparta.team2project.commons.exceptionhandler.CustomException;
import com.sparta.team2project.commons.exceptionhandler.ErrorCode;
import com.sparta.team2project.tripdate.entity.TripDate;
import com.sparta.team2project.tripdate.repository.TripDateRepository;
import com.sparta.team2project.posts.dto.TripDateResponseDto;
import com.sparta.team2project.posts.dto.TripDateOnlyRequestDto;
import com.sparta.team2project.posts.entity.Posts;
import com.sparta.team2project.posts.repository.PostsRepository;
import com.sparta.team2project.schedules.repository.SchedulesRepository;
import com.sparta.team2project.users.UserRepository;
import com.sparta.team2project.users.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TripDateService {
    private final TripDateRepository tripDateRepository;
    private final UserRepository userRepository;
    private final PostsRepository postsRepository;
    private final SchedulesRepository schedulesRepository;

    // TripDate를 조회하는 메서드
    public TripDateResponseDto getTripDate(Long tripDateId) {
        TripDate tripDate = findTripDate(tripDateId);
        return new TripDateResponseDto(tripDate);
    }

    // TripDate 전체를 조회하는 메서드
    public List<TripDateResponseDto> getTripDateAll(Long postId) {
        // 1.postId로 post객체 찾기
        Posts posts = postsRepository.findById(postId).orElseThrow(
                () -> new CustomException(ErrorCode.ID_NOT_MATCH)
        );
        // 2.TripDateRepository에 구현된 findByPosts메서드로 해당 Posts의 TripDate 찾기
        List<TripDate> tripDateList = tripDateRepository.findByPosts(posts);
        // 3. TripDateResponseDto로 변환할 리스트 선언
        List<TripDateResponseDto> TripDateResponseDtoList = new ArrayList<>();
        // 4. TripDateRepository에서 가져온 TripDate 리스트를 TripDateResponseDto로 변환하여 리스트화
        for(TripDate tripDate : tripDateList){
            TripDateResponseDto tripDateResponseDto = new TripDateResponseDto(tripDate);
            TripDateResponseDtoList.add(tripDateResponseDto);
        }
        // 5. TripDateResponseDto 리스트 변환
        return TripDateResponseDtoList;
    }

    // TripDate를 수정하는 메서드
    @Transactional
    public TripDateResponseDto updateTripDate(Long tripDateId, Users users, TripDateOnlyRequestDto tripDateOnlyRequestDto) {
        Users existUser = checkUser(users); // 유저 확인
        checkAuthority(existUser, users);         // 권한 확인
        TripDate tripDate = findTripDate(tripDateId); // 해당 날짜계획 찾기
        tripDate.updateTripDate(tripDateOnlyRequestDto); //선택 날짜계획 업데이트
        return new TripDateResponseDto(tripDate); // ResponseDto에 실어서 반환
//        if(
//                // 수정하려는 스케줄의 날짜가 범위내 있는지 확인
//                (tripDateOnlyRequestDto.getChosenDate().isBefore(tripDate.getPosts().getEndDate())
//                        || tripDateOnlyRequestDto.getChosenDate().isEqual(tripDate.getPosts().getEndDate())) &&
//                (tripDateOnlyRequestDto.getChosenDate().isAfter(tripDate.getPosts().getStartDate())
//                        || tripDateOnlyRequestDto.getChosenDate().isEqual(tripDate.getPosts().getStartDate()))
//
//        ){
            // 범위내 있으면 업데이트

//        }
            // 없으면 날짜 불량 에러 출력
//        else{
//            throw new CustomException(ErrorCode.DATE_NOT_VALID);
//        }
    }

    // TripDate를 삭제하는 메서드
    public MessageResponseDto deleteTripDate(Long tripDateId, Users users) {
        Users existUser = checkUser(users); // 유저 확인
        checkAuthority(existUser, users);         // 권한 확인
        TripDate tripDate = findTripDate(tripDateId);
        tripDateRepository.delete(tripDate);
        MessageResponseDto messageResponseDto = new MessageResponseDto("삭제가 되었습니다.", 200);
        return messageResponseDto;
    }

    // 유저 유효 확인 메서드
    private Users checkUser (Users users) {
        return userRepository.findByEmail(users.getEmail()).
                orElseThrow(() -> new CustomException(ErrorCode.ID_NOT_MATCH));
    }

    // 유저 권한 검사 메서드
    private void checkAuthority(Users existUser,Users users){
        if (!existUser.getUserRole().equals(UserRoleEnum.ADMIN)&&!existUser.getEmail().equals(users.getEmail())) {
            throw new CustomException(ErrorCode.NOT_ALLOWED);
        }
    }

    // TripDate를 Repository에서 찾는 메서드
    private TripDate findTripDate(Long id) {
        return tripDateRepository.findById(id).orElseThrow(
                () -> new CustomException(ErrorCode.ID_NOT_MATCH));
    }

}
