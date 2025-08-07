package batch.kill9.chapter1_step;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

@Slf4j
public class ZombieProcessCleanupTasklet implements Tasklet {
    private final int processesToKill = 10;
    private int killedProcesses = 0;

    /**
     * Tasklet의 execute() 호출마다 트랜잭션을 시작하고, RepeatStatus가 반환되면 트랜잭션을 커밋한다.
     */
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        killedProcesses++;
        log.info("☠️  프로세스 강제 종료... ({}/{})", killedProcesses, processesToKill);

        if (killedProcesses >= processesToKill) {
            log.info("시스템 안정화 완료.");
            return RepeatStatus.FINISHED;
        }
        return RepeatStatus.CONTINUABLE;
    }
}
