package com.filevault.repository;

import com.filevault.entity.File;
import com.filevault.entity.FileAccessType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {
    List<File> findByAdminId(Long adminId);
    List<File> findByCategoryId(Long categoryId);
    List<File> findByAccessType(FileAccessType accessType);
    List<File> findByAdminIdAndAccessType(Long adminId, FileAccessType accessType);
    
    @Query("SELECT f FROM File f WHERE f.accessType = com.filevault.entity.FileAccessType.PUBLIC OR f.accessType = com.filevault.entity.FileAccessType.RESTRICTED")
    List<File> findAllPublicAndRestrictedFiles();
    
    @Query("SELECT f FROM File f WHERE f.admin.id = :adminId AND f.category.id = :categoryId")
    List<File> findByAdminAndCategory(@Param("adminId") Long adminId, @Param("categoryId") Long categoryId);
    
    @Query("SELECT f FROM File f ORDER BY f.id DESC")
    List<File> findAllFiles();
}
