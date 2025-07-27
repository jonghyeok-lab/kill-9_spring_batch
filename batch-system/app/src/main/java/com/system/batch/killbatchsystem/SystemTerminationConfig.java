package com.system.batch.killbatchsystem;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.concurrent.atomic.AtomicInteger;

@Import(BatchConfig.class)
public class SystemTerminationConfig {
    // 두 변수는 Job/Step에 있어서 필수이다.
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private AtomicInteger processsKilled = new AtomicInteger(0);
    private final int TERMINATION_TARGET = 5;

    public SystemTerminationConfig(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
    }

    @Bean
    public Job systemTerminationSimulationJob() {
        return new JobBuilder("systemTerminationSimulateJob", jobRepository)
                .start(enterWorldStep())
                // 이전의 step 이 성공해야 다음 step이 시작된다.
                .next(meetNPCStep())
                .next(defeatProcessStep())
                .next(completeQuestStep())
                .build();
    }

    @Bean
    public Step enterWorldStep() {
        return new StepBuilder("enterWorldStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("시뮬레이션 세계에 접속 환영");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Step meetNPCStep() {
        return new StepBuilder("meetNPCStep", jobRepository)
                .tasklet(((contribution, chunkContext) -> {
                    System.out.println("시스템 관리자 NPC를 만났습니다.");
                    System.out.println("첫 번째 미션: 좀비 프로세스 " + TERMINATION_TARGET + "개 처형하기");
                    return RepeatStatus.FINISHED;
                }), transactionManager)
                .build();
    }

    @Bean
    public Step defeatProcessStep() {
        return new StepBuilder("defeatProcessStep", jobRepository)
                .tasklet(((contribution, chunkContext) -> {
                    int terminated = processsKilled.incrementAndGet();
                    System.out.println("좀비 프로세스 처형 완료 (현재 " + terminated + "/" + TERMINATION_TARGET + ")");
                    if (terminated < TERMINATION_TARGET) {
                        return RepeatStatus.CONTINUABLE;
                    }
                    return RepeatStatus.FINISHED;
                }), transactionManager)
                .build();
    }

    @Bean
    public Step completeQuestStep() {
        return new StepBuilder("completeQuestStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("미션 완료! 좀비 프로세스 " + TERMINATION_TARGET + "개 처형 성공!");
                    System.out.println("보상: kill -9 권한 획득, 시스템 제어 레벨 1 달성");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }
}
