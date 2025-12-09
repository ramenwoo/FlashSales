package com.example.flashsale.service;

import com.example.flashsale.dto.FlashSaleResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class FlashSaleService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String STOCK_KEY_PREFIX = "flashsale:stock:";
    private static final String LOCK_KEY_PREFIX = "flashsale:lock:";
    private static final String PARTICIPANT_KEY_PREFIX = "flashsale:participants:";
    private static final int LOCK_TIMEOUT = 30; // 10분
    private static final String START_TIME_KEY = "flashsale:starttime";
    private static final String UNLOCK_LUA_SCRIPT =
        "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

    private final RedisScript<Long> unlockScript = new DefaultRedisScript<>(UNLOCK_LUA_SCRIPT, Long.class);

    public FlashSaleResponse participate(String userId, String productId) {
        log.info("참여 시도 userId: {}", userId);
        String lockKey = LOCK_KEY_PREFIX  + productId;
        String stockKey = STOCK_KEY_PREFIX + productId;
        String participantKey = PARTICIPANT_KEY_PREFIX + productId;

        // 1. 이미 참여했는지 확인
        if (Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(participantKey, userId))) {
            return FlashSaleResponse.builder()
                .success(false)
                .message("이미 참여하신 상품입니다.")
                .build();
        }

        // 2. SET으로 분산 락 획득 시도
        Boolean lockAcquired = redisTemplate.opsForValue()
        .setIfAbsent(lockKey, userId, LOCK_TIMEOUT, TimeUnit.SECONDS);

        if (Boolean.FALSE.equals(lockAcquired)) {
        return FlashSaleResponse.builder()
            .success(false)
            .message("다른 사용자가 신청 중입니다. 잠시 후 다시 시도해주세요.") // 메시지 수정
            .build();
    }

        try {
            // 3. 재고 확인 및 차감
            Long stock = redisTemplate.opsForValue().decrement(stockKey);

            if (stock == null || stock < 0) {
                // 재고 복구
                redisTemplate.opsForValue().increment(stockKey);
                return FlashSaleResponse.builder()
                    .success(false)
                    .message("마감되었습니다. 다음 기회에 도전해주세요!")
                    .build();
            }

            // 4. 참여자 목록에 추가
            redisTemplate.opsForSet().add(participantKey, userId);

            log.info("FlashSale Success - userId: {}, productId: {}, remainStock: {}",
                userId, productId, stock);

            return FlashSaleResponse.builder()
                .success(true)
                .message("축하합니다! 신청이 완료되었습니다.")
                .remainingStock(stock.intValue())
                .build();

        } finally {
            // 5. 락 해제
                /*redisTemplate.execute(
                unlockScript,
                java.util.Collections.singletonList(lockKey),
                userId
            );*/
        }
    }

    public int getAvailableStock(String productId) {
        String stockKey = STOCK_KEY_PREFIX + productId;
        String stockStr = redisTemplate.opsForValue().get(stockKey);
        return stockStr != null ? Integer.parseInt(stockStr) : 0;
    }

    public boolean isParticipated(String userId, String productId) {
        String participantKey = PARTICIPANT_KEY_PREFIX + productId;
        return Boolean.TRUE.equals(
            redisTemplate.opsForSet().isMember(participantKey, userId)
        );
    }
    public String getSaleStartTime() {
    // 락 획득 실패 시 메시지
    String startTime = redisTemplate.opsForValue().get(START_TIME_KEY);

    // 값이 없으면 기본값 또는 오류 반환
    if (startTime == null) {
        log.warn("Sale start time not set in Redis. Returning default.");
        // 관리자가 설정하지 않은 경우를 대비한 기본값 (예시: 현재 시간 + 1시간)
        return new java.util.Date(System.currentTimeMillis() + 3600000).toInstant().toString();
    }
    return startTime;
}

    // 🌟 관리자용: 세일 시작 시간 설정 메서드 (AdminController에서 사용)
    public void setSaleStartTime(String isoDateTime) {
        redisTemplate.opsForValue().set(START_TIME_KEY, isoDateTime);
        log.info("Sale start time set to: {}", isoDateTime);
    }
    // 관리자용: 재고 초기화
    public void initializeStock(String productId, int quantity) {
        String stockKey = STOCK_KEY_PREFIX + productId;
        redisTemplate.opsForValue().set(stockKey, String.valueOf(quantity));
        log.info("Stock initialized - productId: {}, quantity: {}", productId, quantity);
    }

    // 관리자용: 플래시 세일 초기화 (재고 + 참여자 목록 삭제)
    public void resetFlashSale(String productId) {
        String stockKey = STOCK_KEY_PREFIX + productId;
        String participantKey = PARTICIPANT_KEY_PREFIX + productId;

        redisTemplate.delete(stockKey);
        redisTemplate.delete(participantKey);

        log.info("FlashSale reset - productId: {}", productId);
    }

    public void releaseLock(String userId, String productId) {
    String lockKey = LOCK_KEY_PREFIX + productId;

    // 안전한 락 해제 (Lua Script 재사용)
    // 락의 값(userId)이 일치할 때만 해제됩니다.
    Long result = redisTemplate.execute(
        unlockScript,
        java.util.Collections.singletonList(lockKey),
        userId
    );

    if (result != null && result == 1) {
        log.info("Lock successfully released - userId: {}, productId: {}", userId, productId);
    } else {
        // 🌟 실패 로그에 Redis에 저장된 값까지 확인하도록 추가 (옵션)
        String valueInRedis = redisTemplate.opsForValue().get(lockKey);
        log.warn("Lock release FAILED. Sent userId: {}, Value in Redis: {} (Key: {})",
                 userId, valueInRedis, lockKey);
    }
}

    // 참여자 수 조회
    public long getParticipantsCount(String productId) {
        String participantKey = PARTICIPANT_KEY_PREFIX + productId;
        Long count = redisTemplate.opsForSet().size(participantKey);
        return count != null ? count : 0;
    }
}