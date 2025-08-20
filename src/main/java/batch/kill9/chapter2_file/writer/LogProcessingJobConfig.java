package batch.kill9.chapter2_file.writer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.SystemCommandTasklet;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.LineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Configuration
public class LogProcessingJobConfig {
    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private PlatformTransactionManager transactionManager;

    @Bean
    public Job logProcessingJob(
            Step createDirectoryStep,
            Step logCollectionStep,
            Step logProcessingStep
    ) {
        return new JobBuilder("logProcessingJob", jobRepository)
                .start(createDirectoryStep)
                .next(logCollectionStep)
                .next(logProcessingStep)
                .build();
    }

    @Bean
    public Step createDirectoryStep(SystemCommandTasklet mkdirTasklet) {
        return new StepBuilder("createDirectoryStep", jobRepository)
                .tasklet(mkdirTasklet, transactionManager)
                .build();
    }

    @Bean
    @StepScope
    public SystemCommandTasklet mkdirTasklet(
            @Value("#{jobParameters['date']}") String date
    ) {
        SystemCommandTasklet tasklet = new SystemCommandTasklet();
        tasklet.setWorkingDirectory(System.getProperty("user.home"));

        String collectedLogsPath = "collectd_ecommerce_logs/" + date; // 로그 수집용 디렉토리
        String processedLogsPath = "processed_logs/" + date; // 수집된 로그 처리 결과를 저장할 디렉토리

        // -p 옵션으로 상위 디렉토리까지 생성
        tasklet.setCommand("mkdir", "-p", collectedLogsPath, processedLogsPath, " && ls -al");
        tasklet.setTimeout(3000);
        return tasklet;
    }

    @Bean
    public Step logCollectionStep(SystemCommandTasklet scpTasklet) {
        return new StepBuilder("logCollectionStep", jobRepository)
                .tasklet(scpTasklet, transactionManager)
                .build();
    }

    @Bean
    @StepScope
    public SystemCommandTasklet scpTasklet(
            @Value("#{jobParameters['date']}") String date
    ) {
        SystemCommandTasklet tasklet = new SystemCommandTasklet();
        tasklet.setWorkingDirectory(System.getProperty("user.home"));
        String processedLogsPath = "collected_ecommerce_logs/" + date;

        StringJoiner commandBuilder = new StringJoiner(" && ");
        for (String host : List.of("localhost")) {
            String command = String.format("scp %s:~/ecommerce_logs/%s.log ./%s/%s.log",
                    host, date, processedLogsPath, host);
            commandBuilder.add(command);
        }
        tasklet.setCommand("/bin/sh", "-c", commandBuilder.toString());
        tasklet.setTimeout(10000);
        return tasklet;
    }

    @Bean
    public Step logProcessingStep(
            MultiResourceItemReader<LogEntry> multiResourceItemReader,
            LogEntryProcessor logEntryProcessor,
            FlatFileItemWriter<ProcessedLogEntry> processedLogEntryJsonWriter
    ) {
        return new StepBuilder("logProcessingJob", jobRepository)
                .<LogEntry, ProcessedLogEntry>chunk(10, transactionManager)
                .reader(multiResourceItemReader)
                .processor(logEntryProcessor)
                .writer(processedLogEntryJsonWriter)
                .build();
    }

    @Bean
    @StepScope
    public MultiResourceItemReader<LogEntry> multiResourceItemReader(
            @Value("#{jobParameters['date']}") String date
    ) {
        MultiResourceItemReader<LogEntry> resourceItemReader = new MultiResourceItemReader<>();
        resourceItemReader.setName("multiResourceItemReader");
        resourceItemReader.setResources(getResources(date));
        resourceItemReader.setDelegate(logFileReader());
        return resourceItemReader;
    }

    private Resource[] getResources(String date) {
        try {
            String userHome = System.getProperty("user.home");
            String location = "file:" + userHome + "/collected_ecommerce_logs/" + date + "/*.log";

            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            return resolver.getResources(location);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read collected ecommerce logs", e);
        }
    }

    @Bean
    public FlatFileItemReader<LogEntry> logFileReader() {
        return new FlatFileItemReaderBuilder<LogEntry>()
                .name("logFileReader")
                .delimited()
                .delimiter(",")
                .names("dateTiem", "level", "message")
                .targetType(LogEntry.class)
                .build();
    }

    @Bean
    public LogEntryProcessor logEntryProcessor() {
        return new LogEntryProcessor();
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<ProcessedLogEntry> processedLogEntryJsonWriter(
            @Value("#{jobParameters['date']}") String date
    ) {
        String userHome = System.getProperty("user.home");
        String outputPath = Paths.get(userHome, "processed_logs", date, "processed_logs.jsonl").toString();

        ObjectMapper objectMapper = new ObjectMapper();
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class,
                new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
        objectMapper.registerModule(javaTimeModule);

        return new FlatFileItemWriterBuilder<ProcessedLogEntry>()
                .name("processedLogEntryJsonWriter")
                .resource(new FileSystemResource(outputPath))
                .lineAggregator(new LineAggregator<ProcessedLogEntry>() {
                    @Override
                    public String aggregate(ProcessedLogEntry item) {
                        try {
                            return objectMapper.writeValueAsString(item);
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException("Error converting item to JSON", e);
                        }
                    }
                }).build();
    }


    @Data
    public static class LogEntry {
        private String dateTime;
        private String level;
        private String message;
    }

    @Data
    public static class ProcessedLogEntry {
        private LocalDateTime dateTime;
        private LogLevel level;
        private String message;
        private String errorCode;
    }

    public enum LogLevel {
        INFO, WARN, ERROR, DEBUG, UNKNOWN;

        public static LogLevel fromString(String level) {
            if (level == null || level.trim().isEmpty()) {
                return UNKNOWN;
            }
            try {
                return valueOf(level.toUpperCase());
            } catch (IllegalArgumentException e) {
                return UNKNOWN;
            }
        }
    }

    public static class LogEntryProcessor implements ItemProcessor<LogEntry, ProcessedLogEntry> {
        private final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;
        private final Pattern ERROR_CODE_PATTERN = Pattern.compile("ERROR_CODE\\[(\\w+)]");

        @Override
        public ProcessedLogEntry process(LogEntry item) throws Exception {
            ProcessedLogEntry processedLogEntry = new ProcessedLogEntry();
            processedLogEntry.setDateTime(LocalDateTime.parse(item.getDateTime(), ISO_FORMATTER));
            processedLogEntry.setLevel(LogLevel.fromString(item.getLevel()));
            processedLogEntry.setMessage(item.getMessage());
            processedLogEntry.setErrorCode(extractErrorCode(item.getMessage()));
            return processedLogEntry;
        }

        private String extractErrorCode(String message) {
            if (message == null) {
                return null;
            }

            Matcher matcher = ERROR_CODE_PATTERN.matcher(message);
            if (matcher.find()) {
                return matcher.group(1);
            }

            if (message.contains("ERROR")) {
                return "UNKNOWN_ERROR";
            }
            return null;
        }
    }
}
