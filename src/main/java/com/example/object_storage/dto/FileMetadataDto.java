package com.example.object_storage.dto;

import com.example.object_storage.entity.FilePermission;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FileMetadataDto {
    private Long id;
    private String originalFilename;
    private String contentType;
    private Long fileSize;
    private LocalDateTime uploadTime;
    private String downloadLink;
    private String ownerUsername;
    private FilePermission permission;
}
