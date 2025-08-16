package batch.kill9.chapter2.file;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.stream.Stream;

@Slf4j
@Configuration
public class SystemFailureJobConfig {
    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private PlatformTransactionManager transactionManager;

    @Bean
    public Job systemFailureJob(Step systemFailureStep) {
        return new JobBuilder("systemFailureJob", jobRepository)
                .start(systemFailureStep)
                .build();
    }

    @Bean
    public Step systemFailureStep(
            FlatFileItemReader<SystemFailure> systemFailureItemReader,
            SystemFailureStdoutItemWriter systemFailureStdoutItemWriter
    ) {
        return new StepBuilder("systemFailureStep", jobRepository)
                .<SystemFailure, SystemFailure>chunk(10, transactionManager)
                .reader(systemFailureItemReader)
                .writer(systemFailureStdoutItemWriter)
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<SystemFailure> systemFailureItemReader(
            @Value("#{jobParameters['inputFile']}") String inputFile
    ) {
        return new FlatFileItemReaderBuilder<SystemFailure>() // ItemReader가 반환하는 객체의 타입을 컴파일 시점에 명확히
                .name("systemFailureItemReader") // 유니크한 값
                .resource(new FileSystemResource(inputFile))
                .delimited()
                .delimiter(",")
                .names("errorId",
                        "errorDateTime",
                        "severity",
                        "processId",
                        "errorMessage")
                .targetType(SystemFailure.class) // 런타임에는 Java 타입 소거로 인해 사라지기 때문
                .linesToSkip(1)
//                .comments("#") // 기본값은 # 이며, 해당 문자로 시작하는 라인은 주석으로 간주하고 무시한다.
//                .strict(true) // 기본값은 true이며, 파일 누락 시 예외를 발생시켜 배치를 중단, false면 존재하지 않아도 경고만 남기고 진행.
//                .beanMapperStrict(true) // 기본값 true, FieldSet에 매핑 대상 객체에 존재하지 않는 필드 있을 경우 예외 발생.
                .build();
    }

    @Bean
    public SystemFailureStdoutItemWriter systemFailureStdoutItemWriter() {
        return new SystemFailureStdoutItemWriter();
    }

    public static class SystemFailureStdoutItemWriter implements ItemWriter<SystemFailure> {
        @Override
        public void write(Chunk<? extends SystemFailure> chunk) throws Exception {
            for (SystemFailure failure : chunk) {
                log.info("Processing system failure: {}", failure);
            }
        }
    }

    @Data
    public static class SystemFailure {
        private String errorId;
        private String errorDateTime;
        private String severity;
        private Integer processId;
        private String errorMessage;
    }
}
