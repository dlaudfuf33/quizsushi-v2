package com.cmdlee.quizsushi.minio.service;

import com.cmdlee.quizsushi.global.exception.ErrorCode;
import com.cmdlee.quizsushi.global.exception.GlobalException;
import com.cmdlee.quizsushi.global.config.infra.minio.MinioProperties;
import io.minio.*;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioService {

    private static final String TEMP_PREFIX = "public/tmp/";
    private static final String QUIZ_PREFIX = "public/quiz/";

    private final MinioClient minioClient;
    private final MinioProperties properties;


    public String uploadTempFile(MultipartFile file) {
        String objectName = TEMP_PREFIX + UUID.randomUUID() + extractExtension(file.getOriginalFilename());

        try (InputStream is = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(properties.getBucket())
                            .object(objectName)
                            .stream(is, file.getSize(), -1)
                            .contentType(resolveContentType(file.getOriginalFilename()))
                            .build()
            );
        } catch (Exception e) {
            throw new GlobalException(ErrorCode.FILE_UPLOAD_FAILED, e);
        }

        return buildPublicUrl(objectName);
    }

    public String moveTempFileToQuizFolder(String tmpObjectKey, String mediaKey) {
        String fileName = tmpObjectKey.substring(tmpObjectKey.lastIndexOf("/") + 1);
        String destObjectKey = QUIZ_PREFIX + mediaKey + "/" + fileName;


        try (InputStream is = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(properties.getBucket())
                        .object(tmpObjectKey)
                        .build()
        )) {

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(properties.getBucket())
                            .object(destObjectKey)
                            .stream(is, -1, 10485760)
                            .contentType(resolveContentType(fileName))
                            .build()
            );

            // 기존 tmp 객체 삭제
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(properties.getBucket())
                    .object(tmpObjectKey)
                    .build());

            return buildPublicUrl(destObjectKey);

        } catch (Exception e) {
            throw new GlobalException(ErrorCode.FILE_MOVE_FAILED);
        }
    }

    public void deleteAllWithPrefix(String id) {
        String prefix = QUIZ_PREFIX + id;
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(properties.getBucket())
                            .prefix(prefix)
                            .recursive(true)
                            .build()
            );
            for (Result<Item> result : results) {
                String objectName = result.get().objectName();
                minioClient.removeObject(RemoveObjectArgs.builder()
                        .bucket(properties.getBucket())
                        .object(objectName)
                        .build());
            }
        } catch (Exception e) {
            throw new GlobalException(ErrorCode.FILE_DELETE_FAILED);
        }
    }

    private String resolveContentType(String filename) {
        if (filename == null) return "application/octet-stream";

        String lower = filename.toLowerCase();
        if (lower.endsWith(".mp4")) return "video/mp4";
        if (lower.endsWith(".webm")) return "video/webm";
        if (lower.endsWith(".mov")) return "video/mp4";
        if (lower.endsWith(".mp3")) return "audio/mpeg";
        if (lower.endsWith(".wav")) return "audio/wav";
        if (lower.endsWith(".m4a")) return "audio/mp4";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".png")) return "image/png";
        return "application/octet-stream";
    }

    public String buildPublicUrl(String objectKey) {
        return properties.getEndpoint() + "/" + properties.getBucket() + "/" + objectKey;
    }

    public String rewriteTempMediaLinks(String content, String mediaKey) {
        if (content == null) return "";

        Pattern pattern = Pattern.compile("https?://[^\\s\"')>]+/(quizsushi/)?tmp/[^\\s\"')>]+");
        Matcher matcher = pattern.matcher(content);

        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String tmpUrl = matcher.group();
            String tmpObjectKey = extractObjectKeyFromUrl(tmpUrl);
            String fileName = tmpObjectKey.substring(tmpObjectKey.lastIndexOf("/") + 1);
            String destObjectKey = QUIZ_PREFIX + mediaKey + "/" + fileName;
            String publicUrl = buildPublicUrl(destObjectKey);

            registerMoveAfterCommit(tmpObjectKey, destObjectKey);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(publicUrl));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private String extractObjectKeyFromUrl(String url) {
        try {
            URI uri = new URI(url);
            String path = uri.getPath();
            return path.substring(path.indexOf(TEMP_PREFIX));
        } catch (URISyntaxException e) {
            throw new GlobalException(ErrorCode.URL_PARSE_FAILED, e);
        }
    }

    private String extractExtension(String filename) {
        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf("."));
        }
        return "";
    }

    private void registerMoveAfterCommit(String tmpObjectKey, String destObjectKey) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try (InputStream is = minioClient.getObject(
                            GetObjectArgs.builder()
                                    .bucket(properties.getBucket())
                                    .object(tmpObjectKey)
                                    .build())) {

                        minioClient.putObject(
                                PutObjectArgs.builder()
                                        .bucket(properties.getBucket())
                                        .object(destObjectKey)
                                        .stream(is, -1, 10485760)
                                        .contentType(resolveContentType(destObjectKey))
                                        .build());

                        minioClient.removeObject(RemoveObjectArgs.builder()
                                .bucket(properties.getBucket())
                                .object(tmpObjectKey)
                                .build());

                        log.debug("afterCommit: success move file: {} → {}", tmpObjectKey, destObjectKey);
                    } catch (Exception e) {
                        log.error("afterCommit fail move file: {}", tmpObjectKey, e);
                    }
                }
            });
        } else {
            log.warn("called out of transation");
            moveTempFileToQuizFolder(tmpObjectKey, destObjectKey);
        }
    }
}