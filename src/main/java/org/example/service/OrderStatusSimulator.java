package org.example.service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.OrderStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderStatusSimulator {

    private final SseNotificationService notificationService;
    private final OrderSimulationQueue simulationQueue;

    // 각 사용자별로 현재 어떤 주문 상태인지 추적
    private final Map<String, Integer> userStatusTracker = new ConcurrentHashMap<>();
    private final List<OrderStatus> statusFlow = Arrays.asList(
        OrderStatus.ORDER_RECEIVED,
        OrderStatus.COOKING,
        OrderStatus.OUT_FOR_DELIVERY,
        OrderStatus.DELIVERED
    );

    @Scheduled(fixedRate = 3_000) // 3초마다 실행
    public void processNextOrderInQueue() {
        // 대기열에서 다음 사용자 가져오기
        Optional<String> userIdOptional = simulationQueue.getNextUserId();

        if (userIdOptional.isPresent()) {
            String userId = userIdOptional.get();
            log.info("Processing order for user: {}", userId);
            userStatusTracker.put(userId, 0);
            sendStatusUpdate(userId);
        }
    }

    @Scheduled(fixedRate = 3_000) // 3초마다
    public void advanceOrderStatusForActiveUsers() {
        if (userStatusTracker.isEmpty()) {
            return;
        }

        log.info("Advancing status for {} active orders...", userStatusTracker.size());

        userStatusTracker.forEach((userId, statusIndex) -> {
            // 마지막 상태(DELIVERED)가 아니면 다음 단계로 진행
            if (statusIndex < statusFlow.size() - 1) {
                int nextIndex = statusIndex + 1;
                userStatusTracker.put(userId, nextIndex);
                sendStatusUpdate(userId);
            } else {
                // 배달 완료된 주문은 추적 맵에서 제거
                log.info("Order for user {} is complete. Removing from tracker.", userId);
                userStatusTracker.remove(userId);
            }
        });
    }

    private void sendStatusUpdate(String userId) {
        Integer statusIndex = userStatusTracker.get(userId);
        if (statusIndex != null) {
            OrderStatus currentStatus = statusFlow.get(statusIndex);
            log.info("Sending status update to user {}: {}", userId, currentStatus);
            notificationService.sendToClient(userId, "STATUS_UPDATE", currentStatus);
        }
    }
}
