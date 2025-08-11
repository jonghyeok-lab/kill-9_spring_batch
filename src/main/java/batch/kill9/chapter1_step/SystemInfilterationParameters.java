package batch.kill9.chapter1_step;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 다양한 방식으로 @Value 를 사용하면 된다. Spring 에 걸맞게 다양한 방식으로 DI를 지원해준다.
 */
@StepScope
@Component
public class SystemInfilterationParameters {
    @Value("#{jobParameters[missionName]}")
    private String missionName;
    private int securitLevel;
    private final String operationCommander;

    public SystemInfilterationParameters(@Value("#{jobParameters[operationCommander]}") String operationCommander) {
        this.operationCommander = operationCommander;
    }

    @Value("#{jobParameters[securityLevel]}")
    public void setSecuritLevel(int securitLevel) {
        this.securitLevel = securitLevel;
    }
}
