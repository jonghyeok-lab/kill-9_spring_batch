package batch.kill9.chapter1_step.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BigBrotherStepExecutionListener implements StepExecutionListener {

    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.info("Step 호출 전 . 감시 시작");
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        log.info("스텝 끝난 후 호출.");
        return ExitStatus.COMPLETED;
    }
}
