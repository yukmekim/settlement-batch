package dev.yukmekim.settlement.batch.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
public class SettlementJobListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {
        LocalDate settlementDate = jobExecution.getJobParameters().getLocalDate("settlementDate");
        log.info("정산 Job 시작 - 대상 날짜: {}", settlementDate);
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        LocalDate settlementDate = jobExecution.getJobParameters().getLocalDate("settlementDate");

        switch (jobExecution.getStatus()) {
            case COMPLETED -> log.info("정산 Job 완료 - 대상 날짜: {}", settlementDate);
            case FAILED -> log.error("정산 Job 실패 - 대상 날짜: {}, 원인: {}",
                    settlementDate, jobExecution.getAllFailureExceptions());
            default -> log.warn("정산 Job 종료 - 상태: {}, 대상 날짜: {}",
                    jobExecution.getStatus(), settlementDate);
        }
    }
}
