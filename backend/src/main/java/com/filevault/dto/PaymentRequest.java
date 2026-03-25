package com.filevault.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequest {
    
    @NotNull(message = "File ID cannot be null")
    private Long fileId;
    
    @NotNull(message = "Amount cannot be null")
    @Positive(message = "Amount must be greater than 0")
    private Double amount;
    
    private String paymentMethod; // CREDIT_CARD, DEBIT_CARD, etc.
    
    private String transactionId; // From payment gateway
}
