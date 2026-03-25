package com.filevault.dto;

import com.filevault.entity.FileAccessType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileDetailResponse {
    private Long id;
    private String fileName;
    private String originalFileName;
    private String fileType;
    private Long fileSize;
    private FileAccessType accessType;
    private Double price;
    private String description;
    private Long viewCount;
    private LocalDateTime uploadedAt;
    private String adminEmail;
    private String categoryName;
}
