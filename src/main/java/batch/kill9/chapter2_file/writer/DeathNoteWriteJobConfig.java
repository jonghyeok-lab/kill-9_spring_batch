package batch.kill9.chapter2_file.writer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.MultiResourceItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.builder.MultiResourceItemReaderBuilder;
import org.springframework.batch.item.file.builder.MultiResourceItemWriterBuilder;
import org.springframework.batch.item.file.transform.FieldExtractor;
import org.springframework.batch.item.file.transform.RecordFieldExtractor;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
public class DeathNoteWriteJobConfig {

    @Bean
    public Job deathNoteWriteJob(
            JobRepository jobRepository,
            Step deathNoteWriteStep
    ) {
        return new JobBuilder("deathNoteWriteJob", jobRepository)
                .start(deathNoteWriteStep)
                .build();
    }

    @Bean
    public Step deathNoteWriteStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ListItemReader<DeathNote> deathNoteListReader,
            FlatFileItemWriter<DeathNote> deathNoteWriter
    ) {
        return new StepBuilder("deathNoteWriteStep", jobRepository)
                .<DeathNote, DeathNote>chunk(10, transactionManager)
                .reader(deathNoteListReader)
                .writer(deathNoteWriter)
                .build();
    }

    @Bean
    public ListItemReader<DeathNote> deathNoteListReader() {
        /** 기본 구성 */
//        List<DeathNote> victims = List.of(
//                new DeathNote(
//                        "KILL-001",
//                        "김배치",
//                        "2024-01-25",
//                        "CPU 과부하"),
//                new DeathNote(
//                        "KILL-002",
//                        "사불링",
//                        "2024-01-26",
//                        "JVM 스택오버플로우"),
//                new DeathNote(
//                        "KILL-003",
//                        "박탐묘",
//                        "2024-01-27",
//                        "힙 메모리 고갈")
//        );

        List<DeathNote> deathNotes = new ArrayList<>();
        for (int i = 1; i <= 15; i++) { // 총 15개의 DeathNote 객체 read()
            String id = String.format("KILL-%03d", i);
            LocalDate date = LocalDate.now().plusDays(i);
            deathNotes.add(new DeathNote(
                    id,
                    "피해자" + i,
                    date.toString(),
                    "처형사유" + i
            ));
        }

        return new ListItemReader<>(deathNotes);
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<DeathNote> deathNoteWriter(
            @Value("#{jobParameters['outputDir']}") String outputDir
    ) {
        /** 기본 */
//        return new FlatFileItemWriterBuilder<DeathNote>()
//                .name("deathNoteWriter")
//                .resource(new FileSystemResource(outputDir + "/death_notes.csv"))
//                .delimited()
//                .delimiter(",")
//                .sourceType(DeathNote.class)
//                .names("victimId", "victimName", "executionDate", "causeOfDeath")
//                .headerCallback(writer -> writer.write("처형ID,피해자명,처형일자,사인"))
//                .build();

        /** fieldExtractor 를 사용한 커스텀 , 직접 전달 방식이라 하는데 이 방식은 sourceType 과 names 는 무시된다.*/
//        return new FlatFileItemWriterBuilder<DeathNote>()
//                .name("deathNoteWriter")
//                .resource(new FileSystemResource(outputDir + "/death_notes.csv"))
//                .delimited()
//                .delimiter(",")
//                .fieldExtractor(fieldExtractor())
//                .headerCallback(writer -> writer.write("처형ID,피해자명,처형일자"))
//                .build();

        /** 커스텀 포맷 형식으로 파일 쓰기*/
        return new FlatFileItemWriterBuilder<DeathNote>()
                .name("deathNoteWriter")
                .resource(new FileSystemResource(outputDir + "/death_note_report.txt"))
                .formatted()
                .format("처형 ID: %s | 처형일자: %s | 피해자: %s | 사인: %s")
                .sourceType(DeathNote.class)
                .names("victimId", "executionDate", "victimName", "causeOfDeath")
                .headerCallback(writer -> writer.write("====="))
                .footerCallback(writer -> writer.write("-="))
                .build();
    }

    private RecordFieldExtractor<DeathNote> fieldExtractor() {
        RecordFieldExtractor<DeathNote> fieldExtractor = new RecordFieldExtractor<>(DeathNote.class);
        fieldExtractor.setNames("victimId", "victimName", "executionDate");
        return fieldExtractor;
    }

    /**
     * MultiResourceItemWriter
     */
    @Bean
    public FlatFileItemWriter<DeathNote> delegateItemWriter() {
        return new FlatFileItemWriterBuilder<DeathNote>()
                .name("delegateItemWriter")
                .formatted()
                .format("처형 ID: %s | 처형일자: %s | 피해자: %s | 사인: %s")
                .sourceType(DeathNote.class)
                .names("victimId", "executionDate", "victimName", "causeOfDeath")
                .headerCallback(writer -> writer.write("================= 처형 기록부 ================="))
                .footerCallback(writer -> writer.write("================= 처형 완료 =================="))
                .build();
    }

    @Bean
    @StepScope
    public MultiResourceItemWriter<DeathNote> multiResourceItemWriter(
            @Value("#{jobParameters['outputDir']}") String outputDir
    ) {
        return new MultiResourceItemWriterBuilder<DeathNote>()
                .name("multiResourceItemWriter")
                .resource(new FileSystemResource(outputDir + "/death_note"))
                .itemCountLimitPerResource(10) // 10개가 초과되면 새 파일을 생성한다.
                .delegate(delegateItemWriter())
                .resourceSuffixCreator(index -> String.format("_%03d.txt", index)) // 파일 기본 이름 뒤에 붙을 접미사 정의
                .build();
    }

    @Data
    @AllArgsConstructor
    public static class DeathNote {
        private String victimId;
        private String victimName;
        private String executionDate;
        private String causeOfDeath;
    }

}
