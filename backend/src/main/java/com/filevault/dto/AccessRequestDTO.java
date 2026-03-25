package com.filevault.dto;

import com.filevault.entity.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccessRequestDTO {
    private Long id;
    private Long fileId;
    private Long userId;
    private String fileName;
    private String userEmail;
    private String userName;
    private RequestStatus status;
    private LocalDateTime requestedAt;
    private LocalDateTime respondedAt;
    private String responseReason;
}
