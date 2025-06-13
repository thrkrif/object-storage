package com.example.object_storage.service;

import com.example.object_storage.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * JWT 관련 비즈니스 로직을 처리하는 서비스 클래스
 * JwtUtil을 감싸는 서비스 레이어
 */
@Service
@RequiredArgsConstructor
public class JwtService {
    
    private final JwtUtil jwtUtil;
    
    /**
     * 사용자명으로 JWT 토큰 생성
     * 
     * @param username 사용자명
     * @return JWT 토큰 문자열
     */
    public String generateToken(String username) {
        return jwtUtil.generateToken(username);
    }
    
    /**
     * JWT 토큰에서 사용자명 추출
     * 
     * @param token JWT 토큰
     * @return 사용자명
     */
    public String extractUsername(String token) {
        return jwtUtil.getUsernameFromToken(token);
    }
    
    /**
     * JWT 토큰 유효성 검증
     * 
     * @param token JWT 토큰
     * @return 유효하면 true, 그렇지 않으면 false
     */
    public boolean validateToken(String token) {
        try {
            return jwtUtil.validateToken(token);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Authorization 헤더에서 Bearer 토큰 추출
     * 
     * @param authHeader Authorization 헤더 값
     * @return 추출된 토큰 (Bearer 접두사 제거됨)
     */
    public String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
    
    /**
     * 토큰 만료 여부 확인
     * 
     * @param token JWT 토큰
     * @return 만료되었으면 true, 그렇지 않으면 false
     */
    public boolean isTokenExpired(String token) {
        try {
            // JwtUtil의 validateToken이 만료 검증도 포함하므로
            // 유효하지 않으면 만료되었다고 간주
            return !jwtUtil.validateToken(token);
        } catch (Exception e) {
            return true;
        }
    }
}
