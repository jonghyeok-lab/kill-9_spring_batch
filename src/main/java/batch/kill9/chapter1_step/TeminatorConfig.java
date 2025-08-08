package batch.kill9.chapter1_step;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Configuration
public class TeminatorConfig {

    @Bean
    public Job terminateJob(JobRepository jobRepository, Step terminateStep) {
        return new JobBuilder("terminatorJob", jobRepository)
                .start(terminateStep)
                .build();
    };


    @Bean
    public Step terminateStep(JobRepository jobRepository, PlatformTransactionManager transactionManager, Tasklet terminateTasklet) {
        return new StepBuilder("terminatorStep", jobRepository)
                .tasklet(terminateTasklet, transactionManager)
                .build();
    }

    @Bean
    @StepScope
    public Tasklet terminateTasklet(
            @Value("#{jobParameters['executionDate']}") LocalDate executionDate,
            @Value("#{jobParameters['startTime']}") LocalDateTime startTime
    ) {
        return (contribution, chunkContext) -> {
            log.info("시스템 처형 정보:");
            log.info("처형 예정일: {}", executionDate.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")));
            log.info("작전 개시 시각: {}", startTime.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH시 mm분 ss초")));
            log.info("⚡ {}에 예정된 시스템 정리 작전을 개시합니다.", executionDate);
            log.info("💀 작전 시작 시각: {}", startTime);

            // 작전 진행 상황 추적
            LocalDateTime currentTime = startTime;
            for (int i = 1; i <= 3; i++) {
                currentTime = currentTime.plusHours(1);
                log.info("☠️ 시스템 정리 {}시간 경과... 현재 시각:{}", i, currentTime.format(DateTimeFormatter.ofPattern("HH시 mm분")));
            }

            log.info("🎯 임무 완료: 모든 대상 시스템이 성공적으로 제거되었습니다.");
            log.info("⚡ 작전 종료 시각: {}", currentTime.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH시 mm분 ss초")));


            return RepeatStatus.FINISHED;
        };
    }
}
