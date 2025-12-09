package com.example.flashsale.controller;

import com.example.flashsale.service.FlashSaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/flash-sale")
@RequiredArgsConstructor
public class AdminController {

    private final FlashSaleService flashSaleService;

    @PostMapping("/init-stock")
    public ResponseEntity<Map<String, Object>> initStock(
            @RequestParam String productId,
            @RequestParam int quantity) {

        flashSaleService.initializeStock(productId, quantity);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "재고가 초기화되었습니다.");
        response.put("productId", productId);
        response.put("quantity", quantity);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset")
    public ResponseEntity<Map<String, Object>> reset(
            @RequestParam String productId) {

        flashSaleService.resetFlashSale(productId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "플래시 세일이 초기화되었습니다.");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/participants-count/{productId}")
    public ResponseEntity<Map<String, Object>> getParticipantsCount(
            @PathVariable String productId) {

        long count = flashSaleService.getParticipantsCount(productId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("productId", productId);
        response.put("participantsCount", count);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/set-start-time")
    public ResponseEntity<Map<String, Object>> setStartTime(
            @RequestParam String isoDateTime) {

        flashSaleService.setSaleStartTime(isoDateTime);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "세일 시작 시간이 설정되었습니다.");
        response.put("startTime", isoDateTime);

        return ResponseEntity.ok(response);
    }
}