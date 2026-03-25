package com.filevault.controller;

import com.filevault.dto.AccessRequestDTO;
import com.filevault.dto.AccessRequestActionDTO;
import com.filevault.service.AccessRequestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/access-requests")
public class AccessRequestController {
    
    @Autowired
    private AccessRequestService accessRequestService;
    
    @PostMapping("/request/{fileId}/{userId}")
    public ResponseEntity<?> requestAccess(
            @PathVariable Long fileId,
            @PathVariable Long userId) {
        try {
            AccessRequestDTO request = accessRequestService.requestAccess(fileId, userId);
            return new ResponseEntity<>(request, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Access request error: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Access request failed");
            error.put("message", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }
    
    @PostMapping("/approve")
    public ResponseEntity<?> approveRequest(
            @RequestBody AccessRequestActionDTO action,
            @RequestParam(value = "adminId") Long adminId) {
        try {
            AccessRequestDTO request = accessRequestService.approveRequest(action.getRequestId(), adminId);
            return new ResponseEntity<>(request, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Approve request error: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to approve request");
            error.put("message", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }
    
    @PostMapping("/reject")
    public ResponseEntity<?> rejectRequest(
            @RequestBody AccessRequestActionDTO action,
            @RequestParam(value = "adminId") Long adminId) {
        try {
            AccessRequestDTO request = accessRequestService.rejectRequest(
                    action.getRequestId(), adminId, action.getReason());
            return new ResponseEntity<>(request, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Reject request error: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to reject request");
            error.put("message", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }
    
    @GetMapping("/admin/{adminId}/pending")
    public ResponseEntity<?> getPendingRequestsForAdmin(
            @PathVariable Long adminId) {
        try {
            List<AccessRequestDTO> requests = accessRequestService.getPendingRequestsForAdmin(adminId);
            return new ResponseEntity<>(requests, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Get pending requests error: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve requests");
            error.put("message", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }
    
    @GetMapping("/file/{fileId}")
    public ResponseEntity<?> getRequestsForFile(
            @PathVariable Long fileId,
            @RequestParam(value = "adminId") Long adminId) {
        try {
            List<AccessRequestDTO> requests = accessRequestService.getAllRequestsForFile(fileId, adminId);
            return new ResponseEntity<>(requests, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Get file requests error: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve requests");
            error.put("message", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserRequests(@PathVariable Long userId) {
        try {
            List<AccessRequestDTO> requests = accessRequestService.getUserRequests(userId);
            return new ResponseEntity<>(requests, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Get user requests error: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve requests");
            error.put("message", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }
}
