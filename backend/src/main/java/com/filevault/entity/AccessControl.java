package com.filevault.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "access_control", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"file_id", "user_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccessControl {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id", nullable = false)
    private File file;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AccessType accessType; // SHARED_BY_ADMIN, PURCHASED
    
    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime grantedAt = LocalDateTime.now();
    
    @Column(nullable = true)
    private LocalDateTime expiresAt; // For time-limited access
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    @PrePersist
    protected void onCreate() {
        grantedAt = LocalDateTime.now();
    }
}
