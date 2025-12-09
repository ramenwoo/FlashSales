package com.example.flashsale.controller;

import com.example.flashsale.dto.FlashSaleRequest;
import com.example.flashsale.dto.FlashSaleResponse;
import com.example.flashsale.service.FlashSaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/flash-sale")
@RequiredArgsConstructor
public class FlashSaleController {

    private final FlashSaleService flashSaleService;

    // 플래시 세일 참여 시도
    @PostMapping("/participate")
    public ResponseEntity<FlashSaleResponse> participate(
            @RequestBody FlashSaleRequest request) {
        FlashSaleResponse response = flashSaleService.participate(
            request.getUserId(),
            request.getProductId()
        );
        return ResponseEntity.ok(response);
    }

    // 현재 남은 수량 조회
    @GetMapping("/stock/{productId}")
    public ResponseEntity<Integer> getStock(@PathVariable String productId) {
        int stock = flashSaleService.getAvailableStock(productId);
        return ResponseEntity.ok(stock);
    }

    // 참여 성공 여부 확인
    @GetMapping("/check/{userId}/{productId}")
    public ResponseEntity<Boolean> checkParticipation(
            @PathVariable String userId,
            @PathVariable String productId) {
        boolean participated = flashSaleService.isParticipated(userId, productId);
        return ResponseEntity.ok(participated);
    }

    @GetMapping("/start-time")
    public ResponseEntity<String> getStartTime() {
        String startTime = flashSaleService.getSaleStartTime();
        return ResponseEntity.ok(startTime);
    }

    @PostMapping("/unlock")
    public ResponseEntity<Void> unlock(
            @RequestBody FlashSaleRequest request) {

        flashSaleService.releaseLock(request.getUserId(), request.getProductId());

        // 락 해제는 비동기적으로 처리되므로 성공 응답만 반환
        return ResponseEntity.ok().build();
    }
}