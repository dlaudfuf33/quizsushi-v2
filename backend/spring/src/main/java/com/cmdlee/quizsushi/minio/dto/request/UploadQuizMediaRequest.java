package com.cmdlee.quizsushi.minio.dto.request;

import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Getter
public class UploadQuizMediaRequest {
    private MultipartFile file;
    private String mediaKey;
}
