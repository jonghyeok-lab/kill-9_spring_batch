package batch.kill9;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Kill9Application {

    public static void main(String[] args) {
        // SpringApplication.run 의 결과를 System.exit 으로 처리하는 이유
        // 배치 작업의 성공/실패 상태를 exit code로 외부 시스템에 전달해서
        // 실무에서 배치 모니터링과 제어에 필수적이기 때문. -> 5장 작전2에서 다룬다.
        System.exit(SpringApplication.exit(SpringApplication.run(Kill9Application.class, args)));
    }

}
