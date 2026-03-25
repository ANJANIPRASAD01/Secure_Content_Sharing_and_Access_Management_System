package com.filevault.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccessGrantRequest {
    
    @NotNull(message = "File ID cannot be null")
    private Long fileId;
    
    @NotNull(message = "User ID cannot be null")
    private Long userId;
    
    private Boolean grant; // true to grant, false to revoke
}
