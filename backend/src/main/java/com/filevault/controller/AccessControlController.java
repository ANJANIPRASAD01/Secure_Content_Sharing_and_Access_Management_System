package com.filevault.controller;

import com.filevault.entity.AccessControl;
import com.filevault.service.AccessControlService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/access-control")
public class AccessControlController {
    
    @Autowired
    private AccessControlService accessControlService;
    
    @PostMapping("/grant")
    public ResponseEntity<?> grantAccess(
            @RequestBody Map<String, Object> requestBody) {
        try {
            Long fileId = Long.valueOf(requestBody.get("fileId").toString());
            Long userId = Long.valueOf(requestBody.get("userId").toString());
            Integer timeLimitMonths = requestBody.get("timeLimitMonths") != null 
                ? Integer.valueOf(requestBody.get("timeLimitMonths").toString()) 
                : null;
            
            AccessControl accessControl = accessControlService.grantAccessWithTimeLimit(
                    fileId, 
                    userId, 
                    timeLimitMonths
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Access granted successfully");
            response.put("access", accessControl);
            
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Grant access error: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to grant access");
            error.put("message", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }
    
    @PostMapping("/revoke")
    public ResponseEntity<?> revokeAccess(
            @RequestBody Map<String, Long> requestBody) {
        try {
            Long fileId = requestBody.get("fileId");
            Long userId = requestBody.get("userId");
            Long adminId = requestBody.get("adminId");
            
            accessControlService.revokeAccess(fileId, userId, adminId);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Access revoked successfully");
            
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Revoke access error: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to revoke access");
            error.put("message", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }
    
    @GetMapping("/file/{fileId}")
    public ResponseEntity<?> getAccessesForFile(
            @PathVariable Long fileId,
            @RequestParam Long adminId) {
        try {
            var accesses = accessControlService.getAccessesForFile(fileId, adminId);
            return new ResponseEntity<>(accesses, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Get accesses error: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve accesses");
            error.put("message", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }
}
