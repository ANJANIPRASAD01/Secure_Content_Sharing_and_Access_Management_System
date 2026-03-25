package com.filevault.service;

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

@Slf4j
@Service
@Transactional
public class AccessControlService {
    
    @Autowired
    private AccessControlRepository accessControlRepository;
    
    @Autowired
    private FileRepository fileRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AdminRepository adminRepository;
    
    public AccessControl grantAccess(Long fileId, Long userId, Long adminId) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));
        
        // Verify admin owns the file
        if (!file.getAdmin().getId().equals(adminId)) {
            throw new UnauthorizedAccessException("You don't have permission to grant access to this file");
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Check if access already exists
        Optional<AccessControl> existingAccess = accessControlRepository.findByFileIdAndUserId(fileId, userId);
        if (existingAccess.isPresent()) {
            AccessControl access = existingAccess.get();
            access.setIsActive(true);
            return accessControlRepository.save(access);
        }
        
        AccessControl accessControl = AccessControl.builder()
                .file(file)
                .user(user)
                .accessType(AccessType.SHARED_BY_ADMIN)
                .isActive(true)
                .build();
        
        return accessControlRepository.save(accessControl);
    }

    public AccessControl grantAccessWithTimeLimit(Long fileId, Long userId, Integer timeLimitMonths) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Calculate expiry date
        LocalDateTime expiresAt = null;
        if (timeLimitMonths != null && timeLimitMonths > 0) {
            expiresAt = LocalDateTime.now().plusMonths(timeLimitMonths);
        }
        
        // Check if access already exists
        Optional<AccessControl> existingAccess = accessControlRepository.findByFileIdAndUserId(fileId, userId);
        if (existingAccess.isPresent()) {
            AccessControl access = existingAccess.get();
            access.setIsActive(true);
            access.setExpiresAt(expiresAt);
            return accessControlRepository.save(access);
        }
        
        AccessControl accessControl = AccessControl.builder()
                .file(file)
                .user(user)
                .accessType(AccessType.SHARED_BY_ADMIN)
                .isActive(true)
                .expiresAt(expiresAt)
                .build();
        
        return accessControlRepository.save(accessControl);
    }
    
    public void revokeAccess(Long fileId, Long userId, Long adminId) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));
        
        // Verify admin owns the file
        if (!file.getAdmin().getId().equals(adminId)) {
            throw new UnauthorizedAccessException("You don't have permission to revoke access to this file");
        }
        
        AccessControl accessControl = accessControlRepository.findByFileIdAndUserId(fileId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Access not found"));
        
        accessControl.setIsActive(false);
        accessControlRepository.save(accessControl);
    }
    
    public boolean hasAccess(Long fileId, Long userId) {
        Optional<AccessControl> access = accessControlRepository.findActiveAccess(fileId, userId);
        return access.isPresent();
    }
    
    public List<AccessControl> getAccessesForFile(Long fileId, Long adminId) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));
        
        // Verify admin owns the file
        if (!file.getAdmin().getId().equals(adminId)) {
            throw new UnauthorizedAccessException("You don't have permission to view access for this file");
        }
        
        return accessControlRepository.findByFileIdAndIsActiveTrue(fileId);
    }
    
    public List<AccessControl> getUserAccess(Long userId) {
        return accessControlRepository.findByUserId(userId);
    }
    
    public void addPurchaseAccess(Long fileId, Long userId) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Check if access already exists
        Optional<AccessControl> existingAccess = accessControlRepository.findByFileIdAndUserId(fileId, userId);
        if (existingAccess.isPresent()) {
            AccessControl access = existingAccess.get();
            access.setIsActive(true);
            accessControlRepository.save(access);
            return;
        }
        
        AccessControl accessControl = AccessControl.builder()
                .file(file)
                .user(user)
                .accessType(AccessType.PURCHASED)
                .isActive(true)
                .build();
        
        accessControlRepository.save(accessControl);
    }
}

