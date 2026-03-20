package dev.yukmekim.settlement.dto;

import dev.yukmekim.settlement.domain.order.OrderStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class OrderSummary {

    private final Long amount;
    private final OrderStatus status;
}
