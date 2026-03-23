package dev.yukmekim.settlement.batch;

import dev.yukmekim.settlement.domain.order.Order;
import dev.yukmekim.settlement.domain.order.OrderStatus;
import dev.yukmekim.settlement.domain.settlement.DailySettlement;
import dev.yukmekim.settlement.domain.settlement.SettlementStatus;
import dev.yukmekim.settlement.repository.DailySettlementRepository;
import dev.yukmekim.settlement.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBatchTest
@SpringBootTest
@ActiveProfiles("local")
class DailySettlementJobTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private DailySettlementRepository dailySettlementRepository;

    @BeforeEach
    void setUp() {
        dailySettlementRepository.deleteAll();
        orderRepository.deleteAll();
    }

    @Test
    void 전일_주문을_집계하여_정산_결과를_저장한다() throws Exception {
        // given
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDateTime baseTime = yesterday.atTime(10, 0);

        orderRepository.saveAll(java.util.List.of(
                Order.create("ORD-001", "상품A", 10_000L, OrderStatus.COMPLETED, baseTime),
                Order.create("ORD-002", "상품B", 20_000L, OrderStatus.COMPLETED, baseTime),
                Order.create("ORD-003", "상품C", 30_000L, OrderStatus.COMPLETED, baseTime),
                Order.create("ORD-004", "상품D", 5_000L, OrderStatus.CANCELLED, baseTime),
                Order.create("ORD-005", "상품E", 3_000L, OrderStatus.REFUNDED, baseTime)
        ));

        // when
        JobParameters jobParameters = new JobParametersBuilder()
                .addLocalDate("settlementDate", yesterday)
                .toJobParameters();

        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // then
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        Optional<DailySettlement> result = dailySettlementRepository.findBySettlementDate(yesterday);
        assertThat(result).isPresent();

        DailySettlement settlement = result.get();
        assertThat(settlement.getSettlementDate()).isEqualTo(yesterday);
        assertThat(settlement.getTotalOrderCount()).isEqualTo(5L);
        assertThat(settlement.getTotalAmount()).isEqualTo(60_000L);
        assertThat(settlement.getCancelledCount()).isEqualTo(2L);
        assertThat(settlement.getCancelledAmount()).isEqualTo(8_000L);
        assertThat(settlement.getNetAmount()).isEqualTo(52_000L);
        assertThat(settlement.getStatus()).isEqualTo(SettlementStatus.COMPLETED);
    }

    @Test
    void 당일_주문은_정산에_포함되지_않는다() throws Exception {
        // given
        LocalDate yesterday = LocalDate.now().minusDays(1);

        orderRepository.saveAll(java.util.List.of(
                Order.create("ORD-101", "상품A", 10_000L, OrderStatus.COMPLETED,
                        yesterday.atTime(10, 0)),
                Order.create("ORD-102", "상품B", 50_000L, OrderStatus.COMPLETED,
                        LocalDate.now().atTime(10, 0))  // 당일 주문 - 제외되어야 함
        ));

        // when
        JobParameters jobParameters = new JobParametersBuilder()
                .addLocalDate("settlementDate", yesterday)
                .toJobParameters();

        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // then
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        DailySettlement settlement = dailySettlementRepository.findBySettlementDate(yesterday).get();
        assertThat(settlement.getTotalOrderCount()).isEqualTo(1L);
        assertThat(settlement.getTotalAmount()).isEqualTo(10_000L);
    }
}
