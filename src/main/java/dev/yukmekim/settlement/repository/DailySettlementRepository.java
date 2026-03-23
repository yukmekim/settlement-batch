package dev.yukmekim.settlement.repository;

import dev.yukmekim.settlement.domain.settlement.DailySettlement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface DailySettlementRepository extends JpaRepository<DailySettlement, Long> {

    Optional<DailySettlement> findBySettlementDate(LocalDate settlementDate);

    boolean existsBySettlementDate(LocalDate settlementDate);

    void deleteBySettlementDate(LocalDate settlementDate);
}
