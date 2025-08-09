package org.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
@RequiredArgsConstructor
public class SseNotificationService {

    private static final Long EMITTER_TIMEOUT = 10 * 60 * 1000L; // 10분
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;
    private final OrderSimulationQueue simulationQueue; // 실제 서버에서는 db 사용

    public SseEmitter subscribe(String userId) {
        SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT);

        if (emitters.containsKey(userId)) {
            emitters.get(userId).complete();
        }
        emitters.put(userId, emitter);

        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));
        emitter.onError(e -> emitters.remove(userId));

        // 시뮬레이션 큐에 userId 추가
        simulationQueue.addUser(userId);

        sendEventToClient(emitter, "CONNECT", "Connection successful. Your order will be processed soon.");

        return emitter;
    }

    @Scheduled(fixedRate = 15_000)
    public void sendHeartbeat() {
        if (emitters.isEmpty()) {
            return;
        }
        String heartbeatPayload = "tick " + System.currentTimeMillis();
        emitters.forEach((userId, emitter) -> {
            sendEventToClient(emitter, "HEARTBEAT", heartbeatPayload);
        });
    }

    public void sendToAllClients(String eventType, Object payload) {
        if (emitters.isEmpty()) return;
        log.info("Sending event '{}' to all {} clients.", eventType, emitters.size());
        emitters.forEach((userId, emitter) -> sendEventToClient(emitter, eventType, payload));
    }

    public void sendToClient(String userId, String eventType, Object payload) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            sendEventToClient(emitter, eventType, payload);
        } else {
            log.warn("No active emitter found for userId: {}", userId);
        }
    }

    private void sendEventToClient(SseEmitter emitter, String eventType, Object payload) {
        try {
            Map<String, Object> eventData = Map.of("type", eventType, "payload", payload);
            String jsonEventData = objectMapper.writeValueAsString(eventData);
            emitter.send(SseEmitter.event().data(jsonEventData));
        } catch (IOException e) {
            log.error("Failed to send SSE event to client: {}", e.getMessage());
        }
    }
}
