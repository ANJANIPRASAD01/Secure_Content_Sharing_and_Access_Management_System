package com.filevault.repository;

import com.filevault.entity.Payment;
import com.filevault.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByUserId(Long userId);
    List<Payment> findByAdminId(Long adminId);
    List<Payment> findByFileId(Long fileId);
    List<Payment> findByStatus(PaymentStatus status);
    Optional<Payment> findByTransactionId(String transactionId);
    
    @Query("SELECT p FROM Payment p WHERE p.user.id = :userId AND p.file.id = :fileId AND p.status = 'COMPLETED'")
    Optional<Payment> findCompletedPayment(@Param("userId") Long userId, @Param("fileId") Long fileId);
    
    @Query("SELECT p FROM Payment p WHERE p.user.id = :userId AND p.admin.id = :adminId AND p.status = 'COMPLETED'")
    List<Payment> findUserPaymentsToAdmin(@Param("userId") Long userId, @Param("adminId") Long adminId);
    
    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.admin.id = :adminId AND p.status = 'COMPLETED'")
    Double getTotalEarningsByAdmin(@Param("adminId") Long adminId);
}
