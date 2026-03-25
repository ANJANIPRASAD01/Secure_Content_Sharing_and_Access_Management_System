package com.filevault.repository;

import com.filevault.entity.AccessControl;
import com.filevault.entity.AccessType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccessControlRepository extends JpaRepository<AccessControl, Long> {
    Optional<AccessControl> findByFileIdAndUserId(Long fileId, Long userId);
    List<AccessControl> findByFileId(Long fileId);
    List<AccessControl> findByUserId(Long userId);
    List<AccessControl> findByFileIdAndIsActiveTrue(Long fileId);
    
    @Query("SELECT ac FROM AccessControl ac WHERE ac.file.id = :fileId AND ac.user.id = :userId AND ac.isActive = true")
    Optional<AccessControl> findActiveAccess(@Param("fileId") Long fileId, @Param("userId") Long userId);
    
    @Query("SELECT ac FROM AccessControl ac WHERE ac.file.id = :fileId AND ac.accessType = :accessType")
    List<AccessControl> findByFileAndAccessType(@Param("fileId") Long fileId, @Param("accessType") AccessType accessType);
    
    @Query("SELECT ac FROM AccessControl ac WHERE ac.user.id = :userId AND ac.accessType = :accessType AND ac.isActive = true")
    List<AccessControl> findUserAccessByType(@Param("userId") Long userId, @Param("accessType") AccessType accessType);
}
