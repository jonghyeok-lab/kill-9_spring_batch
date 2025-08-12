package batch.kill9.chapter1_step.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BigBrotherJobExecutionListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("잡 시작 전 호출. 시스템 통제 시작");
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        log.info("잡 호출 후 호출. 자원 정리");
        log.info("시스템 상태:{}", jobExecution.getStatus());
    }
}
