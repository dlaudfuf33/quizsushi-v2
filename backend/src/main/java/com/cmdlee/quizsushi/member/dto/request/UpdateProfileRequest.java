package com.cmdlee.quizsushi.member.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class UpdateProfileRequest {
    private String nickname;
    private String birth;
    private String gender;

    public LocalDate getLocalDateBirth(){
        if (this.birth == null || this.birth.isBlank()) {
            return null;
        }
        return LocalDate.parse(this.birth);
    }
}
