package dev.yukmekim.settlement.domain.settlement;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DailySettlementRepository extends JpaRepository<DailySettlement, Long> {
}
