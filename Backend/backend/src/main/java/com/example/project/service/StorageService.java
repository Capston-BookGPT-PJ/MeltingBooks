package com.example.project.service;

import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.objectstorage.requests.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class StorageService {

    private final ObjectStorageClient objectStorageClient;

    // 실제 환경값과 반드시 일치시켜 주세요
    private static final String NAMESPACE = "axfmxzr7cohq";
    private static final String BUCKET    = "melting-images";
    private static final String REGION    = "ap-osaka-1";

    /** 공용 업로드: 전달받은 objectName 그대로 업로드하고 같은 경로의 URL 반환 */
    public String upload(MultipartFile file, String objectName) {
        try {
            String contentType = (file.getContentType() == null || file.getContentType().isBlank())
                    ? "application/octet-stream"
                    : file.getContentType();

            PutObjectRequest request = PutObjectRequest.builder()
                    .namespaceName(NAMESPACE)
                    .bucketName(BUCKET)
                    .objectName(objectName)
                    .contentType(contentType)
                    .contentLength(file.getSize())
                    .putObjectBody(new ByteArrayInputStream(file.getBytes()))
                    .build();

            objectStorageClient.putObject(request);

            return publicUrl(objectName);
        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 실패", e);
        }
    }

    /** (선택) 프로필/리뷰용 헬퍼 – 내부적으로 upload 호출 */
    public String uploadProfileImage(Long userId, MultipartFile file) {
        String objectName = "profile/" + userId + "_" + file.getOriginalFilename();
        return upload(file, objectName);
    }

    public String uploadReviewImage(Long reviewId, MultipartFile file) {
        String objectName = "review/" + reviewId + "_" + file.getOriginalFilename();
        return upload(file, objectName);
    }

    private String publicUrl(String objectName) {
        // 반드시 putObject에 사용한 objectName과 동일해야 함
        return String.format(
                "https://objectstorage.%s.oraclecloud.com/n/%s/b/%s/o/%s",
                REGION, NAMESPACE, BUCKET, objectName
        );
    }
}
