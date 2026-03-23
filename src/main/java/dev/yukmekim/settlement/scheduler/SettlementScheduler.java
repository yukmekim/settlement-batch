package dev.yukmekim.settlement.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementScheduler {

    private final JobLauncher jobLauncher;

    @Qualifier("dailySettlementJob")
    private final Job dailySettlementJob;

    @Scheduled(cron = "${settlement.schedule.cron}")
    public void runDailySettlement() {
        LocalDate targetDate = LocalDate.now().minusDays(1);
        log.info("정산 배치 시작 - 대상 날짜: {}", targetDate);

        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLocalDate("settlementDate", targetDate)
                    .toJobParameters();

            jobLauncher.run(dailySettlementJob, jobParameters);
        } catch (Exception e) {
            log.error("정산 배치 실행 실패 - 대상 날짜: {}", targetDate, e);
        }
    }
}
