package com.filevault.service;

import com.filevault.dto.AccessRequestDTO;
import com.filevault.entity.*;
import com.filevault.exception.ResourceNotFoundException;
import com.filevault.exception.UnauthorizedAccessException;
import com.filevault.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class AccessRequestService {
    
    @Autowired
    private AccessRequestRepository accessRequestRepository;
    
    @Autowired
    private FileRepository fileRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AdminRepository adminRepository;
    
    @Autowired
    private AccessControlRepository accessControlRepository;
    
    public AccessRequestDTO requestAccess(Long fileId, Long userId) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        if (file.getAccessType() != FileAccessType.RESTRICTED) {
            throw new RuntimeException("Access request can only be made for restricted files");
        }
        
        // Check if request already exists
        Optional<AccessRequest> existing = accessRequestRepository.findByFileIdAndUserIdAndStatus(
                fileId, userId, RequestStatus.PENDING);
        if (existing.isPresent()) {
            throw new RuntimeException("You already have a pending access request for this file");
        }
        
        // Check if already approved
        Optional<AccessControl> approved = accessControlRepository.findActiveAccess(fileId, userId);
        if (approved.isPresent()) {
            throw new RuntimeException("You already have access to this file");
        }
        
        AccessRequest request = AccessRequest.builder()
                .file(file)
                .user(user)
                .status(RequestStatus.PENDING)
                .build();
        
        accessRequestRepository.save(request);
        return convertToDTO(request);
    }
    
    public AccessRequestDTO approveRequest(Long requestId, Long adminId) {
        AccessRequest request = accessRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));
        
        // Verify admin owns the file
        if (!request.getFile().getAdmin().getId().equals(adminId)) {
            throw new UnauthorizedAccessException("You don't have permission to approve this request");
        }
        
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new RuntimeException("Request has already been responded to");
        }
        
        request.setStatus(RequestStatus.APPROVED);
        request.setRespondedAt(LocalDateTime.now());
        accessRequestRepository.save(request);
        
        // Grant access
        AccessControl access = AccessControl.builder()
                .file(request.getFile())
                .user(request.getUser())
                .accessType(AccessType.SHARED_BY_ADMIN)
                .build();
        accessControlRepository.save(access);
        
        log.info("Access request {} approved", requestId);
        return convertToDTO(request);
    }
    
    public AccessRequestDTO rejectRequest(Long requestId, Long adminId, String reason) {
        AccessRequest request = accessRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));
        
        // Verify admin owns the file
        if (!request.getFile().getAdmin().getId().equals(adminId)) {
            throw new UnauthorizedAccessException("You don't have permission to reject this request");
        }
        
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new RuntimeException("Request has already been responded to");
        }
        
        request.setStatus(RequestStatus.REJECTED);
        request.setRespondedAt(LocalDateTime.now());
        request.setResponseReason(reason);
        accessRequestRepository.save(request);
        
        log.info("Access request {} rejected", requestId);
        return convertToDTO(request);
    }
    
    public List<AccessRequestDTO> getPendingRequestsForAdmin(Long adminId) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));
        
        List<File> adminFiles = fileRepository.findByAdminId(adminId);
        
        return adminFiles.stream()
                .flatMap(file -> accessRequestRepository.findByFileIdAndStatus(file.getId(), RequestStatus.PENDING).stream())
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<AccessRequestDTO> getAllRequestsForFile(Long fileId, Long adminId) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));
        
        if (!file.getAdmin().getId().equals(adminId)) {
            throw new UnauthorizedAccessException("You don't have permission to view requests for this file");
        }
        
        return accessRequestRepository.findByFileId(fileId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<AccessRequestDTO> getUserRequests(Long userId) {
        return accessRequestRepository.findByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    private AccessRequestDTO convertToDTO(AccessRequest request) {
        return AccessRequestDTO.builder()
                .id(request.getId())
                .fileId(request.getFile().getId())
                .userId(request.getUser().getId())
                .fileName(request.getFile().getOriginalFileName())
                .userEmail(request.getUser().getEmail())
                .userName(request.getUser().getFirstName() + " " + request.getUser().getLastName())
                .status(request.getStatus())
                .requestedAt(request.getRequestedAt())
                .respondedAt(request.getRespondedAt())
                .responseReason(request.getResponseReason())
                .build();
    }
}
