package com.filevault.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccessRequestActionDTO {
    @NotNull(message = "Request ID is required")
    private Long requestId;
    
    @NotNull(message = "Action is required - APPROVED or REJECTED")
    private String action; // APPROVED or REJECTED
    
    private String reason; // Optional reason for rejection
}
