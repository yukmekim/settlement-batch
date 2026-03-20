package dev.yukmekim.settlement.batch.reader;

import dev.yukmekim.settlement.domain.order.Order;
import dev.yukmekim.settlement.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderReader implements ItemReader<Order> {

    private final OrderRepository orderRepository;

    private Iterator<Order> iterator;

    @Override
    public Order read() throws UnexpectedInputException, ParseException, NonTransientResourceException {
        if (iterator == null) {
            LocalDate yesterday = LocalDate.now().minusDays(1);
            LocalDateTime start = yesterday.atStartOfDay();
            LocalDateTime end = yesterday.plusDays(1).atStartOfDay();

            List<Order> orders = orderRepository.findByOrderedAtBetween(start, end);
            iterator = orders.iterator();
        }

        return iterator.hasNext() ? iterator.next() : null;
    }
}
