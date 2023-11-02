package com.sparta.team2project.schedules.dto;

import com.sparta.team2project.schedules.entity.SchedulesCategory;
import lombok.Getter;

@Getter
public class SchedulesRequestDto {
//    private LocalDate chosenDate;
    private SchedulesCategory schedulesCategory;
//    private String details;
    private int costs;
    private String placeName;
    private String contents;
//    private LocalTime startTime;
//    private LocalTime endTime;
    private String timeSpent;
    private String referenceURL;
    private String x;
    private String y;
}
