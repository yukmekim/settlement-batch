package dev.yukmekim.settlement.domain.order;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByOrderedAtBetween(LocalDateTime start, LocalDateTime end);
}
