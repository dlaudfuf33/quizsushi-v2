package com.cmdlee.quizsushi.minio.controller;

import com.cmdlee.quizsushi.minio.service.MinioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class MinioController {

    private final MinioService minioService;

    @PostMapping("/uploads/temp-media")
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file) throws Exception {
        String url = minioService.uploadTempFile(file);
        return ResponseEntity.ok(url);
    }
}
