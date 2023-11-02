package com.sparta.team2project.profile.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class AboutMeRequestDto {
    @Size(max = 200)
    private String aboutMe;
}
