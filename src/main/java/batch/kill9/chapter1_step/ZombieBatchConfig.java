package batch.kill9.chapter1_step;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@RequiredArgsConstructor
@Configuration
public class ZombieBatchConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Tasklet zombieProcessCleanupTasklet() {
        return new ZombieProcessCleanupTasklet();
    }

    @Bean
    public Step zombiCleanupStep() {
        return new StepBuilder("zombieCleanupStep", jobRepository)
                .tasklet(zombieProcessCleanupTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Job zombieCleanupJob() {
        return new JobBuilder("zombieCleanupJob", jobRepository)
                .start(zombiCleanupStep())
                .build();
    }

}
