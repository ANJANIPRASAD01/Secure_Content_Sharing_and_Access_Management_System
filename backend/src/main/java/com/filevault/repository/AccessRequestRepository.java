package com.filevault.repository;

import com.filevault.entity.AccessRequest;
import com.filevault.entity.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccessRequestRepository extends JpaRepository<AccessRequest, Long> {
    List<AccessRequest> findByFileId(Long fileId);
    List<AccessRequest> findByUserId(Long userId);
    List<AccessRequest> findByFileIdAndUserId(Long fileId, Long userId);
    List<AccessRequest> findByStatus(RequestStatus status);
    List<AccessRequest> findByFileIdAndStatus(Long fileId, RequestStatus status);
    List<AccessRequest> findByUserIdAndStatus(Long userId, RequestStatus status);
    Optional<AccessRequest> findByFileIdAndUserIdAndStatus(Long fileId, Long userId, RequestStatus status);
}
