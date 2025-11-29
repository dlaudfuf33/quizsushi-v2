package com.cmdlee.quizsushi.quiz.dto.response;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Author {
    private String id;
    private String nickName;
    private String avatar;
}