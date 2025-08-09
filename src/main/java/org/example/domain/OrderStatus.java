package org.example.domain;

import java.util.Arrays;

public enum OrderStatus {
    ORDER_RECEIVED, // 주문 접수
    COOKING,        // 조리 중
    OUT_FOR_DELIVERY, // 배달 중
    DELIVERED       // 배달 완료
    ;

    public static OrderStatus nextStatus(OrderStatus status) {
        int ordinal = status.ordinal();

        return Arrays.stream(values())
                .filter(s -> s.ordinal() == (ordinal + 1) % 4)
                .findFirst()
                .orElse(OrderStatus.ORDER_RECEIVED);
    }
}
