package dev.yukmekim.settlement.batch.writer;

import dev.yukmekim.settlement.domain.order.OrderStatus;
import dev.yukmekim.settlement.domain.settlement.DailySettlement;
import dev.yukmekim.settlement.domain.settlement.DailySettlementRepository;
import dev.yukmekim.settlement.dto.OrderSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class SettlementWriter implements ItemWriter<OrderSummary> {

    private final DailySettlementRepository dailySettlementRepository;

    private long totalOrderCount;
    private long totalAmount;
    private long cancelledCount;
    private long cancelledAmount;
    private LocalDate settlementDate;

    @BeforeStep
    public void beforeStep(StepExecution stepExecution) {
        totalOrderCount = 0;
        totalAmount = 0;
        cancelledCount = 0;
        cancelledAmount = 0;
        settlementDate = LocalDate.now().minusDays(1);

        if (dailySettlementRepository.existsBySettlementDate(settlementDate)) {
            dailySettlementRepository.deleteBySettlementDate(settlementDate);
        }
    }

    @Override
    public void write(Chunk<? extends OrderSummary> chunk) {
        for (OrderSummary order : chunk) {
            totalOrderCount++;
            if (order.getStatus() == OrderStatus.COMPLETED) {
                totalAmount += order.getAmount();
            } else {
                cancelledCount++;
                cancelledAmount += order.getAmount();
            }
        }
    }

    @AfterStep
    @Transactional
    public ExitStatus saveSettlement(StepExecution stepExecution) {
        DailySettlement settlement = DailySettlement.builder()
                .settlementDate(settlementDate)
                .totalOrderCount(totalOrderCount)
                .totalAmount(totalAmount)
                .cancelledCount(cancelledCount)
                .cancelledAmount(cancelledAmount)
                .netAmount(totalAmount - cancelledAmount)
                .build();

        settlement.complete();
        dailySettlementRepository.save(settlement);
        return ExitStatus.COMPLETED;
    }
}
