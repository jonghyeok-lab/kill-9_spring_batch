package batch.kill9.learn;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

@Configuration
public class BasicConfiguration {

    @Bean
    public Job dataTerminateJob(Step terminateStep, JobRepository jobRepository) {
        return new JobBuilder("dataTerminateJob", jobRepository)
                .start(terminateStep)
                .build();
    }

    @Bean
    public Step terminateStep(ItemReader<String> itemReader, ItemWriter<String> itemWriter, JobRepository jobRepository, DataSourceTransactionManager transactionManager) {
        return new StepBuilder("terminateStep", jobRepository)
                .<String, String>chunk(10, transactionManager)
                .reader(itemReader)
                .writer(itemWriter)
                .build();
    }

    @Bean
    public ItemReader<String> itemReader() {
        return new JdbcPagingItemReader<>();
    }

    @Bean
    public ItemWriter<String> itemWriter() {
        return new JdbcBatchItemWriter<>();
    }
}
