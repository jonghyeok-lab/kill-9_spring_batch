package batch.kill9.chapter1_step;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import java.io.File;
import java.util.Arrays;

@Slf4j
public class DeleteOldFilesTasklet implements Tasklet {
    private final String path;
    private final int daysOld;

    public DeleteOldFilesTasklet(String path, int daysOld) {
        this.path = path;
        this.daysOld = daysOld;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        File dir = new File(path);
        long cutoffTime = System.currentTimeMillis() - (daysOld * 1000L * 60 * 60 * 24);

        File[] files = dir.listFiles();
        if (files != null) {
            Arrays.stream(files)
                    .filter(file -> file.lastModified() < cutoffTime)
                    .forEach(file -> {
                        if (file.delete()) {
                            log.info("파일 삭제: {}", file.getName());
                        } else {
                            log.info("파일 삭제 실패: {}", file.getName());
                        }
                    });
        }

        return RepeatStatus.FINISHED;
    }

    @Configuration
    static class FileCleanupBatchConfig {

        @Bean
        public Tasklet deleteOldFilesTasklet() {
            // 30일 이상 지난 파일 삭제
            return new DeleteOldFilesTasklet("/path/tmp", 30);
        }

        @Bean
        public Step deleteOldRecordStep(JdbcTemplate jdbcTemplate, DataSourceTransactionManager transactionManager, JobRepository jobRepository) {
            return new StepBuilder("deleteOldRecordStep", jobRepository)
                    .tasklet(((contribution, chunkContext) -> {
                        int deleted = jdbcTemplate.update("DELETE FROM logs WHERE created < NOW() - INTERVAL 7 DAY");
                        log.info("{}개의 오래된 레코드 삭제", deleted);
                        return RepeatStatus.FINISHED;
                    }), transactionManager)
                    .build();
        }
    }


}
