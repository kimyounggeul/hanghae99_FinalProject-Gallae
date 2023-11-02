package com.sparta.team2project.tripdate.controller;

import com.sparta.team2project.commons.dto.MessageResponseDto;
import com.sparta.team2project.commons.security.UserDetailsImpl;
import com.sparta.team2project.tripdate.service.TripDateService;
import com.sparta.team2project.posts.dto.TripDateResponseDto;
import com.sparta.team2project.posts.dto.TripDateOnlyRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "여행 날짜 관련 API", description = "여행 날짜 관련 API")
@RequestMapping("/api")
public class TripDateController {
    private final TripDateService tripDateService;

    // TripDate 조회 메서드
    @Operation(summary = "세부(단일) 여행 날짜 조회", description = "세부 여행 날짜 조회 api 입니다.")
    @GetMapping("/tripDate/{tripDateId}")
    public TripDateResponseDto getTripDate(@PathVariable("tripDateId") Long tripDateId){
        return tripDateService.getTripDate(tripDateId);
    }

    // TripDate 전체 조회 메서드
    @Operation(summary = "게시글별 여행 날짜 조회", description = "게시글별 여행 날짜 조회 api 입니다.")
    @GetMapping("/posts/{postId}/tripDate")
    public List<TripDateResponseDto> getTripDateAll(@PathVariable("postId") Long postId){
        return tripDateService.getTripDateAll(postId);
    }

    // TripDate를 수정하는 메서드
    @Operation(summary = "여행 날짜 수정", description = "여행 날짜 수정 api 입니다.")
    @PutMapping("/tripDate/{tripDateId}")
    public TripDateResponseDto updateTripDate(@PathVariable("tripDateId") Long tripDateId,
                                              @AuthenticationPrincipal UserDetailsImpl userDetails,
                                              @RequestBody TripDateOnlyRequestDto tripDateOnlyRequestDto
                                         ) {
        return tripDateService.updateTripDate(tripDateId, userDetails.getUsers(), tripDateOnlyRequestDto);
    }

    // TripDate를 삭제하는 메서드
    @Operation(summary = "여행 날짜 삭제", description = "여행 날짜 삭제 api 입니다.")
    @DeleteMapping("/tripDate/{tripDateId}")
    public MessageResponseDto deleteTripDate(@PathVariable("tripDateId") Long tripDateId,
                                             @AuthenticationPrincipal UserDetailsImpl userDetails){
        return tripDateService.deleteTripDate(tripDateId, userDetails.getUsers());
    }
}
