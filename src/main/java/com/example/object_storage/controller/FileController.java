package com.example.object_storage.controller;

import com.example.object_storage.dto.FileMetadataDto;
import com.example.object_storage.dto.FileUploadResponse;
import com.example.object_storage.entity.FilePermission;
import com.example.object_storage.entity.User;
import com.example.object_storage.service.FileStorageService;
import com.example.object_storage.service.UserService;
import com.example.object_storage.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class FileController {
    
    private final FileStorageService fileStorageService;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("Authorization") String authHeader) {
        
        try {
            User user = getUserFromToken(authHeader);
            FileUploadResponse response = fileStorageService.uploadFile(file, user);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/files")
    public ResponseEntity<?> getFiles(@RequestHeader("Authorization") String authHeader) {
        try {
            User user = getUserFromToken(authHeader);
            List<FileMetadataDto> files = fileStorageService.getUserFiles(user);
            return ResponseEntity.ok(files);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/files/{id}")
    public ResponseEntity<?> getFileMetadata(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {
        
        try {
            User user = getUserFromToken(authHeader);
            Optional<FileMetadataDto> file = fileStorageService.getFileMetadata(id, user);
            
            if (file.isPresent()) {
                return ResponseEntity.ok(file.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PutMapping("/files/{id}/permission")
    public ResponseEntity<?> updateFilePermission(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            @RequestHeader("Authorization") String authHeader) {
        
        try {
            User user = getUserFromToken(authHeader);
            FilePermission permission = FilePermission.valueOf(request.get("permission"));
            String password = request.get("password");
            
            boolean updated = fileStorageService.updateFilePermission(id, user, permission, password);
            
            if (updated) {
                return ResponseEntity.ok(Map.of("message", "Permission updated successfully"));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @DeleteMapping("/files/{id}")
    public ResponseEntity<?> deleteFile(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {
        
        try {
            User user = getUserFromToken(authHeader);
            boolean deleted = fileStorageService.deleteFile(id, user);
            
            if (deleted) {
                return ResponseEntity.ok(Map.of("message", "File deleted successfully"));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    private User getUserFromToken(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String username = jwtUtil.getUsernameFromToken(token);
        return userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}