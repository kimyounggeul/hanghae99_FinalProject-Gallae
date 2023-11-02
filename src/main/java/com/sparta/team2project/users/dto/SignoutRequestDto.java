package com.sparta.team2project.users.dto;

import lombok.Getter;
@Getter
public class SignoutRequestDto {

    private String email;
    private String password;
    private boolean admin = false;
    private String adminToken = "";
}