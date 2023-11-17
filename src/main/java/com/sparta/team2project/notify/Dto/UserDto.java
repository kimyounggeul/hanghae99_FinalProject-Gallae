package com.sparta.team2project.notify.Dto;

import com.sparta.team2project.users.Users;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserDto {
    private Long UsersId;

    public UserDto(Users users) {
        this.UsersId = users.getId();
    }
}
