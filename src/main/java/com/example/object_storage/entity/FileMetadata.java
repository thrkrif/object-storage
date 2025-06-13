package com.example.object_storage.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "file_metadata")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileMetadata {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String originalFilename;
    
    @Column(nullable = false)
    private String storedFilename;
    
    @Column(nullable = false)
    private String contentType;
    
    @Column(nullable = false)
    private Long fileSize;
    
    @Column(name = "upload_time")
    private LocalDateTime uploadTime;
    
    @Column(name = "download_link", unique = true)
    private String downloadLink;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;
    
    @Enumerated(EnumType.STRING)
    private FilePermission permission = FilePermission.PRIVATE;
    
    private String accessPassword;
    
    @PrePersist
    protected void onCreate() {
        uploadTime = LocalDateTime.now();
    }
}