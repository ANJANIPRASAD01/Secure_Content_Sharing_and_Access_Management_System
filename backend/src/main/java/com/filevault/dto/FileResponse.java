package com.filevault.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileResponse {
    
    private Long id;
    private String fileName;
    private String originalFileName;
    private Long fileSize;
    private String fileType;
    private String accessType;
    private String categoryName;
    private Long adminId;
    private String adminName;
    private Double price;
    private String description;
    private LocalDateTime uploadedAt;
    private Boolean hasAccess; // Whether current user has access
}
