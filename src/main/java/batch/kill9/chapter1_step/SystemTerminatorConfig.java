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

@Slf4j
@Configuration
public class SystemTerminatorConfig {
    @Bean
    public Job processTerminatorJob(JobRepository jobRepository, Step terminatorStep) {
        return new JobBuilder("processTerminatorJob", jobRepository)
                .start(terminatorStep)
                .build();
    }

    @Bean
    public Step terminatorStep(JobRepository jobRepository, PlatformTransactionManager transactionManager, Tasklet terminatorTasklet) {
        return new StepBuilder("terminatorStep", jobRepository)
                .tasklet(terminatorTasklet, transactionManager)
                .build();
    }

    /**
     * @Value 를 사용해 잡 파라미터를 받으려면 @StepScope 가 필요하다.
     * Batch의 Scope 는 뒤에서 다룬다
     */
    @Bean
    @StepScope
    public Tasklet terminatorTasklet(
            @Value("#{jobParameters['terminatorId']}") String terminatorId,
            @Value("#{jobParameters['targetCount']}") Integer targetCount
    ) {
        return (contribution, chunkContext) -> {
            log.info("시스템 종결자");
            log.info("ID {}", terminatorId);
            log.info("타겟 수: {}", targetCount);

            for (int i = 1; i <= targetCount; i++) {
                log.info("💀 프로세스 {} 종료 완료!", i);
            }

            return RepeatStatus.FINISHED;
        };
    }

}
