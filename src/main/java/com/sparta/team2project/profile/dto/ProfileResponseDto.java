package com.sparta.team2project.profile.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sparta.team2project.profile.Profile;
import com.sparta.team2project.users.Users;
import lombok.Getter;

@Getter
public class ProfileResponseDto {
    private String email; // 고정 값(수정 불가)
    @JsonIgnore
    private String password; //숨겨야하는 값
    private String nickName;
    private String profileImg;
    private String aboutMe;

    public ProfileResponseDto(Users users, Profile profile) {
        this.email = users.getEmail();
        this.password = users.getPassword();
        this.nickName = users.getNickName();
        this.profileImg = users.getProfileImg();
        this.aboutMe = profile.getAboutMe();
    }
}
