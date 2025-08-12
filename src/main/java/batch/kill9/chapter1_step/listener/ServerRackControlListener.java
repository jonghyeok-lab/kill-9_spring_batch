package batch.kill9.chapter1_step.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.stereotype.Component;

@Slf4j
//@Component
public class ServerRackControlListener {
    @BeforeStep
    public void access(StepExecution stepExecution) {
        log.info("스텝 전 호출");
    }

    @AfterStep
    public ExitStatus end(StepExecution stepExecution) {
        log.info("스텝 후 호출");
        return new ExitStatus("POWER_DOWN");
    }
}
