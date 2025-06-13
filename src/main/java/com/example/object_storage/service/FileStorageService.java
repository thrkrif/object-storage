package com.example.object_storage.service;

import com.example.object_storage.dto.FileMetadataDto;
import com.example.object_storage.dto.FileUploadResponse;
import com.example.object_storage.entity.FileMetadata;
import com.example.object_storage.entity.FilePermission;
import com.example.object_storage.entity.User;
import com.example.object_storage.repository.FileMetadataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileStorageService {
    
    private final FileMetadataRepository fileMetadataRepository;
    
    @Value("${file.upload.dir:./uploads}")
    private String uploadDir;
    
    public FileUploadResponse uploadFile(MultipartFile file, User owner) throws IOException {
        // 저장 디렉토리 생성
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // 파일명 생성
        String originalFilename = file.getOriginalFilename();
        String storedFilename = UUID.randomUUID().toString() + "_" + originalFilename;
        String downloadLink = UUID.randomUUID().toString();
        
        // 파일 저장
        Path filePath = uploadPath.resolve(storedFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // 메타데이터 저장
        FileMetadata metadata = new FileMetadata();
        metadata.setOriginalFilename(originalFilename);
        metadata.setStoredFilename(storedFilename);
        metadata.setContentType(file.getContentType());
        metadata.setFileSize(file.getSize());
        metadata.setDownloadLink(downloadLink);
        metadata.setOwner(owner);
        
        metadata = fileMetadataRepository.save(metadata);
        
        return new FileUploadResponse(
            metadata.getId(),
            metadata.getOriginalFilename(),
            metadata.getDownloadLink(),
            metadata.getFileSize(),
            metadata.getContentType()
        );
    }
    
    public List<FileMetadataDto> getUserFiles(User user) {
        return fileMetadataRepository.findByOwner(user)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public Optional<FileMetadataDto> getFileMetadata(Long fileId, User user) {
        return fileMetadataRepository.findByIdAndOwner(fileId, user)
                .map(this::convertToDto);
    }
    
    public boolean updateFilePermission(Long fileId, User user, FilePermission permission, String password) {
        Optional<FileMetadata> fileOpt = fileMetadataRepository.findByIdAndOwner(fileId, user);
        
        if (fileOpt.isPresent()) {
            FileMetadata file = fileOpt.get();
            file.setPermission(permission);
            if (permission == FilePermission.PASSWORD_PROTECTED) {
                file.setAccessPassword(password);
            } else {
                file.setAccessPassword(null);
            }
            fileMetadataRepository.save(file);
            return true;
        }
        
        return false;
    }
    
    public boolean deleteFile(Long fileId, User user) throws IOException {
        Optional<FileMetadata> fileOpt = fileMetadataRepository.findByIdAndOwner(fileId, user);
        
        if (fileOpt.isPresent()) {
            FileMetadata file = fileOpt.get();
            
            // 실제 파일 삭제
            Path filePath = Paths.get(uploadDir).resolve(file.getStoredFilename());
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
            
            // 메타데이터 삭제
            fileMetadataRepository.delete(file);
            return true;
        }
        
        return false;
    }
    
    public Resource downloadFile(String downloadLink, String password) throws IOException {
        Optional<FileMetadata> fileOpt = fileMetadataRepository.findByDownloadLink(downloadLink);
        
        if (fileOpt.isEmpty()) {
            throw new RuntimeException("File not found");
        }
        
        FileMetadata file = fileOpt.get();
        
        // 권한 확인
        if (file.getPermission() == FilePermission.PRIVATE) {
            throw new RuntimeException("Access denied");
        }
        
        if (file.getPermission() == FilePermission.PASSWORD_PROTECTED) {
            if (password == null || !password.equals(file.getAccessPassword())) {
                throw new RuntimeException("Invalid password");
            }
        }
        
        Path filePath = Paths.get(uploadDir).resolve(file.getStoredFilename());
        if (!Files.exists(filePath)) {
            throw new RuntimeException("File not found on disk");
        }
        
        return new UrlResource(filePath.toUri());
    }
    
    public Optional<FileMetadata> getFileByDownloadLink(String downloadLink) {
        return fileMetadataRepository.findByDownloadLink(downloadLink);
    }
    
    private FileMetadataDto convertToDto(FileMetadata file) {
        FileMetadataDto dto = new FileMetadataDto();
        dto.setId(file.getId());
        dto.setOriginalFilename(file.getOriginalFilename());
        dto.setContentType(file.getContentType());
        dto.setFileSize(file.getFileSize());
        dto.setUploadTime(file.getUploadTime());
        dto.setDownloadLink(file.getDownloadLink());
        dto.setOwnerUsername(file.getOwner().getUsername());
        dto.setPermission(file.getPermission());
        return dto;
    }
}