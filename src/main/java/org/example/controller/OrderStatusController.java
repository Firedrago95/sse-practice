package org.example.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/sse")
public class OrderStatusController {

    @GetMapping(value = "/connect", produces = "text/event-stream")
    public SseEmitter connect() {
        return null; // 구현 필요
    }
}
