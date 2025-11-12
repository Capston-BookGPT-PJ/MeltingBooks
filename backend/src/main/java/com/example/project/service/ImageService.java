package com.example.project.service;

import com.example.project.entity.Review;
import com.example.project.entity.User;
import com.example.project.exception.ReviewNotFoundException;
import com.example.project.exception.UserNotFoundException;
import com.example.project.repository.ReviewRepository;
import com.example.project.repository.UserRepository;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.objectstorage.requests.PutObjectRequest;
import com.oracle.bmc.objectstorage.responses.PutObjectResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final ObjectStorageClient objectStorageClient;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;

    private static final String NAMESPACE_NAME = "axfmxzr7cohq";
    private static final String IMAGE_BUCKET = "melting-images";

    @Transactional
    public String uploadProfileImage(Long userId, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        String fileName = "profile/" + generateFileName(userId, file.getOriginalFilename());
        String imageUrl = uploadToOci(IMAGE_BUCKET, fileName, file);

        user.setProfileImageUrl(imageUrl);
        userRepository.save(user);

        return imageUrl;
    }

    @Transactional
    public List<String> uploadReviewImages(Long reviewId, List<MultipartFile> files) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId));

        List<String> imageUrls = new ArrayList<>();
        for (MultipartFile file : files) {
            String fileName = "review/" + generateFileName(reviewId, file.getOriginalFilename());
            String imageUrl = uploadToOci(IMAGE_BUCKET, fileName, file);
            imageUrls.add(imageUrl);
        }

        review.getReviewImageUrls().addAll(imageUrls);
        reviewRepository.save(review);

        return imageUrls;
    }

    private String uploadToOci(String bucketName, String objectName, MultipartFile file) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .namespaceName(NAMESPACE_NAME)
                    .bucketName(bucketName)
                    .objectName(objectName)
                    .putObjectBody(new ByteArrayInputStream(file.getBytes()))
                    .contentLength(file.getSize())
                    .build();

            objectStorageClient.putObject(request);

            return String.format("https://objectstorage.%s.oraclecloud.com/n/%s/b/%s/o/%s",
                    "ap-osaka-1", NAMESPACE_NAME, bucketName, objectName);

        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 실패", e);
        }
    }

    private String generateFileName(Long id, String originalFilename) {
        String ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        return id + "_" + UUID.randomUUID() + ext;
    }
}

