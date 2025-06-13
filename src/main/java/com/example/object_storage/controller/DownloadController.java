package com.example.object_storage.controller;

import com.example.object_storage.entity.FileMetadata;
import com.example.object_storage.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/download")
@RequiredArgsConstructor
public class DownloadController {
    
    private final FileStorageService fileStorageService;
    
    @GetMapping("/{linkId}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable String linkId,
            @RequestParam(required = false) String password) {
        
        try {
            Resource resource = fileStorageService.downloadFile(linkId, password);
            Optional<FileMetadata> fileOpt = fileStorageService.getFileByDownloadLink(linkId);
            
            if (fileOpt.isEmpty()) {
                return ResponseEntity.notFound().build(); // 404
            }
            
            FileMetadata file = fileOpt.get();
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(file.getContentType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                           "attachment; filename=\"" + file.getOriginalFilename() + "\"")
                    .body(resource);
                    
        } catch (RuntimeException e) {
            // 메시지에 따른 적절한 HTTP 상태 코드 반환
            String message = e.getMessage();
            
            if (message.contains("File not found")) {
                return ResponseEntity.notFound().build(); // 404
            } else if (message.contains("Access denied") || 
                      message.contains("Invalid password")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // 403
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // 500
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // 500
        }
    }
}