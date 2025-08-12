package batch.kill9.chapter1_step;

import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.config.Task;

public class ExecutionContextConfig {

    @Bean
    @JobScope
    public Tasklet systemDetruction(
            @Value("#{jobExecutionContext['previousSystemState']}") String prevState
    ) {
        return ((contribution, chunkContext) -> {
            System.out.println();
            return RepeatStatus.FINISHED;
        });
    }

    @Bean
    @StepScope
    public Tasklet infiltration(
            @Value("#{stepExecutionContext['targetSystemStatus']}") String targetSystemStatus
    ) {
        return (contribution, chunkContext) -> {
            System.out.println();
            return RepeatStatus.FINISHED;
        };
    }
}
