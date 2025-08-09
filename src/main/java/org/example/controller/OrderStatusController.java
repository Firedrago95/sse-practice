package org.example.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.example.service.SseNotificationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/sse")
public class OrderStatusController {

    private final SseNotificationService sseNotificationService;

    public OrderStatusController(SseNotificationService sseNotificationService) {
        this.sseNotificationService = sseNotificationService;
    }

    @GetMapping(value = "/connect", produces = "text/event-stream")
    public SseEmitter connect(@RequestParam String userId, HttpServletResponse response) {
        // Nginx 프록시 환경에서 버퍼링 방지
        response.setHeader("X-Accel-Buffering", "no");
        return sseNotificationService.subscribe(userId);
    }
}