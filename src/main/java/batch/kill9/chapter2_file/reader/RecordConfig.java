package batch.kill9.chapter2_file.reader;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;

public class RecordConfig {

    @Bean
    @StepScope
    public FlatFileItemReader<SystemDeath> reader(
            @Value("#{jobParameters['inputFile']}") String inputFile
    ) {
        return new FlatFileItemReaderBuilder<SystemDeath>()
                .name("systemKill")
                .resource(new FileSystemResource(inputFile))
                .delimited()
                .delimiter(",")
                .names("command", "cpu", "status")
                .targetType(SystemDeath.class) // record 전달 시, RecordFieldSetMapper 를 사용
                .linesToSkip(1)
                .build();
    }


    public static record SystemDeath(
            String command,
            int cpu,
            String status
    ) {}
}
