package org.example.service;

import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.springframework.stereotype.Component;


@Component
public class OrderSimulationQueue {

    private final ConcurrentLinkedQueue<String> userIdQueue = new ConcurrentLinkedQueue<>();

    public void addUser(String userId) {
        userIdQueue.add(userId);
    }

    public Optional<String> getNextUserId() {
        return Optional.ofNullable(userIdQueue.poll());
    }
}
