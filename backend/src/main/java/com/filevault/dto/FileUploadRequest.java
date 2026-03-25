package com.filevault.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileUploadRequest {
    
    @NotBlank(message = "File name cannot be blank")
    private String fileName;
    
    @NotNull(message = "Category ID cannot be null")
    private Long categoryId;
    
    @NotBlank(message = "Access type cannot be blank")
    private String accessType; // PUBLIC, PRIVATE, RESTRICTED
    
    private String description;
    
    private Double price; // For restricted files
}
