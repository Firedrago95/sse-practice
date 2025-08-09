package org.example.domain;

public enum OrderStatus {
    ORDER_RECEIVED, // 주문 접수
    COOKING,        // 조리 중
    OUT_FOR_DELIVERY, // 배달 중
    DELIVERED       // 배달 완료
}
