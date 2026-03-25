package com.filevault.service;

import com.filevault.dto.FileResponse;
import com.filevault.entity.Admin;
import com.filevault.entity.File;
import com.filevault.exception.ResourceNotFoundException;
import com.filevault.repository.AdminRepository;
import com.filevault.repository.FileRepository;
import com.filevault.repository.PaymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class AdminService {
    
    @Autowired
    private AdminRepository adminRepository;
    
    @Autowired
    private FileRepository fileRepository;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private FileService fileService;
    
    public Admin getAdminById(Long adminId) {
        return adminRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));
    }
    
    public Admin getAdminByEmail(String email) {
        return adminRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));
    }
    
    public Admin updateAdminProfile(Long adminId, String firstName, String lastName, String phoneNumber) {
        Admin admin = getAdminById(adminId);
        if (firstName != null) admin.setFirstName(firstName);
        if (lastName != null) admin.setLastName(lastName);
        if (phoneNumber != null) admin.setPhoneNumber(phoneNumber);
        return adminRepository.save(admin);
    }
    
    public List<FileResponse> getAdminFiles(Long adminId) {
        List<File> files = fileRepository.findByAdminId(adminId);
        return files.stream()
                .map(fileService::convertToFileResponse)
                .collect(Collectors.toList());
    }
    
    public Double getTotalEarnings(Long adminId) {
        return paymentRepository.getTotalEarningsByAdmin(adminId);
    }
    
    public Long getTotalFilesUploaded(Long adminId) {
        return (long) fileRepository.findByAdminId(adminId).size();
    }
    
    public Long getTotalAccessesByAdmin(Long adminId) {
        List<File> files = fileRepository.findByAdminId(adminId);
        return files.stream()
                .mapToLong(file -> file.getId())
                .count();
    }
}
