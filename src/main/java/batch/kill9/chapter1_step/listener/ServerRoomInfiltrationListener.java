package batch.kill9.chapter1_step.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.annotation.AfterJob;
import org.springframework.batch.core.annotation.BeforeJob;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ServerRoomInfiltrationListener {
    @BeforeJob
    public void infiltrateServerRoom(JobExecution jobExecution) {
        log.info("잡 호출 전 리스너");
    }

    @AfterJob
    public void outfiltrateServerRoom(JobExecution jobExecution) {
        log.info("잡 호출 후 리스너");
    }
}
