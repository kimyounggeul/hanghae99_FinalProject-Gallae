package com.sparta.team2project.schedules.dto;

import com.sparta.team2project.schedules.entity.SchedulesCategory;
import lombok.Getter;

import java.util.List;

@Getter
public class CreateSchedulesRequestDto {
    private List<SchedulesRequestDto> schedulesList;

}

