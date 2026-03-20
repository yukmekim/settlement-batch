package dev.yukmekim.settlement.domain.settlement;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface DailySettlementRepository extends JpaRepository<DailySettlement, Long> {

    boolean existsBySettlementDate(LocalDate settlementDate);

    void deleteBySettlementDate(LocalDate settlementDate);
}
