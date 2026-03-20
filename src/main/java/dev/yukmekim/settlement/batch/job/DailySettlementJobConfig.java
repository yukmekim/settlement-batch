package dev.yukmekim.settlement.batch.job;

import dev.yukmekim.settlement.batch.processor.SettlementProcessor;
import dev.yukmekim.settlement.batch.reader.OrderReader;
import dev.yukmekim.settlement.batch.writer.SettlementWriter;
import dev.yukmekim.settlement.domain.order.Order;
import dev.yukmekim.settlement.dto.OrderSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class DailySettlementJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final OrderReader orderReader;
    private final SettlementProcessor settlementProcessor;
    private final SettlementWriter settlementWriter;

    @Value("${settlement.chunk-size:500}")
    private int chunkSize;

    @Bean
    public Job dailySettlementJob() {
        return new JobBuilder("dailySettlementJob", jobRepository)
                .start(dailySettlementStep())
                .build();
    }

    @Bean
    public Step dailySettlementStep() {
        return new StepBuilder("dailySettlementStep", jobRepository)
                .<Order, OrderSummary>chunk(chunkSize, transactionManager)
                .reader(orderReader)
                .processor(settlementProcessor)
                .writer(settlementWriter)
                .listener(settlementWriter)
                .build();
    }
}
