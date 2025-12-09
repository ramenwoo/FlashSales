package com.example.flashsale.controller;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
// @CrossOrigin(origins = "*")  ← 이거 삭제!
public class TestController {

    private final RedisTemplate<String, String> redisTemplate;

    @GetMapping("/redis")
    public ResponseEntity<Map<String, Object>> testRedis() {
        Map<String, Object> response = new HashMap<>();

        try {
            redisTemplate.opsForValue().set("test:key", "test-value");
            String value = redisTemplate.opsForValue().get("test:key");

            response.put("success", true);
            response.put("message", "Redis 연결 성공");
            response.put("testValue", value);

            redisTemplate.delete("test:key");

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Redis 연결 실패: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "서버가 정상 작동 중입니다.");
        return ResponseEntity.ok(response);
    }
}