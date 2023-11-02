package com.sparta.team2project.email.dto;

import jakarta.validation.constraints.Email;
import lombok.Getter;

@Getter
public class ValidNumberRequestDto {
    private int validNumber;

    @Email(message = "이메일 형식에 맞지 않습니다.")
    private String email;

}
