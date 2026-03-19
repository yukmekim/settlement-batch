package dev.yukmekim.settlement.domain.settlement;

import dev.yukmekim.settlement.domain.common.BaseTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "daily_settlement")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailySettlement extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private LocalDate settlementDate;

    @Column(nullable = false)
    private Long totalOrderCount;

    @Column(nullable = false)
    private Long totalAmount;

    @Column(nullable = false)
    private Long cancelledCount;

    @Column(nullable = false)
    private Long cancelledAmount;

    @Column(nullable = false)
    private Long netAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SettlementStatus status;

    private LocalDateTime settledAt;

    @Builder
    public DailySettlement(LocalDate settlementDate, Long totalOrderCount, Long totalAmount,
                           Long cancelledCount, Long cancelledAmount, Long netAmount) {
        this.settlementDate = settlementDate;
        this.totalOrderCount = totalOrderCount;
        this.totalAmount = totalAmount;
        this.cancelledCount = cancelledCount;
        this.cancelledAmount = cancelledAmount;
        this.netAmount = netAmount;
        this.status = SettlementStatus.IN_PROGRESS;
    }

    public void complete() {
        this.status = SettlementStatus.COMPLETED;
        this.settledAt = LocalDateTime.now();
    }

    public void fail() {
        this.status = SettlementStatus.FAILED;
    }
}
