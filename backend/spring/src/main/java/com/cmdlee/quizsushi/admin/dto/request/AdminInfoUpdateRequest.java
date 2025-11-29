package com.cmdlee.quizsushi.admin.dto.request;

import lombok.Data;

@Data
public class AdminInfoUpdateRequest {
    private String newAlias;
    private String newRawPassword;
}
