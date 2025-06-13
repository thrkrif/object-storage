package com.example.object_storage.dto;

import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class FileUploadResponse {
    private Long fileId;
    private String originalFilename;
    private String downloadLink;
    private Long fileSize;
    private String contentType;
}