package com.example.project.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationService {
    public void pushToUser(Long userId, String title, String body) {
        // TODO: Firebase Cloud Messaging 연동 지점
        log.info("PUSH -> userId: {}, title: {}, body: {}", userId, title, body);
    }
}
