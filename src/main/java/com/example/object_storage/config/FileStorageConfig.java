package com.example.object_storage.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 파일 스토리지 관련 설정을 담당하는 Configuration 클래스
 */
@Configuration
public class FileStorageConfig implements WebMvcConfigurer {
    
    @Value("${file.upload.dir:./uploads}")
    private String uploadDir;
    
    @Value("${file.upload.max-size:10MB}")
    private String maxFileSize;
    
    @Value("${file.upload.max-request-size:10MB}")
    private String maxRequestSize;
    
    /**
     * 애플리케이션 시작 시 업로드 디렉토리 초기화
     */
    @Bean
    public String initializeUploadDirectory() {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                System.out.println("Upload directory created: " + uploadPath.toAbsolutePath());
            } else {
                System.out.println("Upload directory already exists: " + uploadPath.toAbsolutePath());
            }
            return uploadPath.toAbsolutePath().toString();
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory: " + uploadDir, e);
        }
    }
    
    /**
     * 정적 리소스 핸들러 설정
     * 업로드된 파일들을 웹에서 접근할 수 있도록 설정
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 업로드된 파일을 /files/** URL로 접근할 수 있도록 설정
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:" + Paths.get(uploadDir).toAbsolutePath() + "/")
                .setCachePeriod(3600); // 1시간 캐시
                
        // 기본 정적 리소스 설정 유지
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(31536000); // 1년 캐시
    }
    
    /**
     * CORS 설정
     * 프론트엔드에서 API 호출할 수 있도록 설정
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
                
        registry.addMapping("/download/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
    
    /**
     * 파일 업로드 관련 설정 정보를 제공하는 Bean
     */
    @Bean
    public FileUploadProperties fileUploadProperties() {
        FileUploadProperties properties = new FileUploadProperties();
        properties.setUploadDir(uploadDir);
        properties.setMaxFileSize(maxFileSize);
        properties.setMaxRequestSize(maxRequestSize);
        return properties;
    }
    
    /**
     * 파일 업로드 설정 정보를 담는 클래스
     */
    public static class FileUploadProperties {
        private String uploadDir;
        private String maxFileSize;
        private String maxRequestSize;
        
        // Getters and Setters
        public String getUploadDir() {
            return uploadDir;
        }
        
        public void setUploadDir(String uploadDir) {
            this.uploadDir = uploadDir;
        }
        
        public String getMaxFileSize() {
            return maxFileSize;
        }
        
        public void setMaxFileSize(String maxFileSize) {
            this.maxFileSize = maxFileSize;
        }
        
        public String getMaxRequestSize() {
            return maxRequestSize;
        }
        
        public void setMaxRequestSize(String maxRequestSize) {
            this.maxRequestSize = maxRequestSize;
        }
    }
    
    /**
     * 허용된 파일 확장자 검증 메서드
     */
    public static boolean isValidFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return false;
        }
        
        String[] allowedExtensions = {
            // 이미지
            ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp", ".svg",
            // 문서
            ".pdf", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx", ".txt",
            // 압축
            ".zip", ".rar", ".7z", ".tar", ".gz",
            // 비디오
            ".mp4", ".avi", ".mov", ".wmv", ".flv", ".webm",
            // 오디오
            ".mp3", ".wav", ".flac", ".aac", ".ogg"
        };
        
        String lowerFilename = filename.toLowerCase();
        for (String ext : allowedExtensions) {
            if (lowerFilename.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 파일 크기가 허용 범위 내인지 검증
     */
    public static boolean isValidFileSize(long fileSize) {
        // 최대 10MB (10 * 1024 * 1024 bytes)
        long maxSize = 10L * 1024 * 1024;
        return fileSize > 0 && fileSize <= maxSize;
    }
}