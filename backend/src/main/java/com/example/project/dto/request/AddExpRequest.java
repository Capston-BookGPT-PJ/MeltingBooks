// DTO
package com.example.project.dto.request;

import lombok.Data;

@Data
public class AddExpRequest {
    private Long userId;
    private int points;  // JSON에서 points로 맞춰서 보낼 것
}
