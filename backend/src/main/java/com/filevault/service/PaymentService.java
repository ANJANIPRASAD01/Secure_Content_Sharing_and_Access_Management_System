package com.filevault.service;

import com.filevault.dto.PaymentRequest;
import com.filevault.entity.*;
import com.filevault.exception.ResourceNotFoundException;
import com.filevault.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
public class PaymentService {
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private FileRepository fileRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AdminRepository adminRepository;
    
    @Autowired
    private AccessControlService accessControlService;
    
    public Payment initiatePayment(Long fileId, Long userId, PaymentRequest paymentRequest) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Verify file is restricted and has a price
        if (file.getAccessType() != FileAccessType.RESTRICTED || file.getPrice() == null) {
            throw new RuntimeException("This file is not available for purchase");
        }
        
        // Check if user already has access
        Optional<Payment> existingPayment = paymentRepository.findCompletedPayment(userId, fileId);
        if (existingPayment.isPresent()) {
            throw new RuntimeException("User already has access to this file");
        }
        
        Payment payment = Payment.builder()
                .user(user)
                .admin(file.getAdmin())
                .file(file)
                .amount(file.getPrice())
                .status(PaymentStatus.PENDING)
                .transactionId(paymentRequest.getTransactionId())
                .paymentMethod(paymentRequest.getPaymentMethod())
                .build();
        
        return paymentRepository.save(payment);
    }
    
    public Payment completePayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new RuntimeException("Payment has already been processed");
        }
        
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setCompletedAt(LocalDateTime.now());
        payment = paymentRepository.save(payment);
        
        // Grant access to the user
        accessControlService.addPurchaseAccess(payment.getFile().getId(), payment.getUser().getId());
        
        return payment;
    }
    
    public Payment failPayment(Long paymentId, String reason) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        
        payment.setStatus(PaymentStatus.FAILED);
        return paymentRepository.save(payment);
    }
    
    public List<Payment> getPaymentsByUser(Long userId) {
        return paymentRepository.findByUserId(userId);
    }
    
    public List<Payment> getPaymentsByAdmin(Long adminId) {
        return paymentRepository.findByAdminId(adminId);
    }
    
    public List<Payment> getPaymentsByFile(Long fileId) {
        return paymentRepository.findByFileId(fileId);
    }
    
    public Double getTotalEarningsByAdmin(Long adminId) {
        Double earnings = paymentRepository.getTotalEarningsByAdmin(adminId);
        return earnings != null ? earnings : 0.0;
    }
    
    public Payment getPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
    }
    
    public List<Payment> getCompletedPayments() {
        return paymentRepository.findByStatus(PaymentStatus.COMPLETED);
    }
}
