package com.goorm.shoppingmall.domain.order.service;

import com.goorm.shoppingmall.domain.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
public class OrderNumberGenerator {

    private final OrderRepository orderRepository;
    private final AtomicInteger sequence = new AtomicInteger(0);

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd");

    // 생성 예시: ORD-20250419-000001
    public String generate() {
        String datePart = LocalDateTime.now().format(FORMATTER);
        String seqPart;

        // 중복되지 않는 번호 생성될 때까지 반복
        do {
            int seq = sequence.incrementAndGet() % 1000000;
            seqPart = String.format("%06d", seq);
        } while (orderRepository.existsByOrderNumber("ORD-" + datePart + "-" + seqPart));

        return "ORD-" + datePart + "-" + seqPart;
    }
}