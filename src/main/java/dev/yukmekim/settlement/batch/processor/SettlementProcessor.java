package dev.yukmekim.settlement.batch.processor;

import dev.yukmekim.settlement.domain.order.Order;
import dev.yukmekim.settlement.dto.OrderSummary;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class SettlementProcessor implements ItemProcessor<Order, OrderSummary> {

    @Override
    public OrderSummary process(Order order) {
        return new OrderSummary(order.getAmount(), order.getStatus());
    }
}
