# Spring Batch의 Step
- 청크 지향 처리
- 테스크릿 지향 처리

## 테스크릿 지향 처리
- Spring Batch 의 가장 간단한(기본) Step 구현 방식으로 읽기/처리/쓰기가 아닌 것.
- 대부분 함수 호출 하나로 끝날법한 단순한 작업들.
  - 매일 새벽 불필요한 로그 파일 삭제
  - 특정 디렉토리에서 오래된 파일을 아카이브(압축)
  - 사용자에게 단순한 알림/이메일 발송
  - 외부 API 호출 후 결과를 단순히 저장하거나 로깅
- Tasklet 인터페이스의 `execute()` 를 구현해 Batch에게 넘기기만 하면 된다.

### ResourcelessTransactionManager
- no-op(아무것도 하지 않는) 방식으로 동작하는 PlatformTransactionManager 구현체로 사용 시, 불필요한 DB 트랜잭션 처리를 생략 가능
- 언제 사용?
  - Tasklet 이 파일을 정리하고, 외부 API 호출하고, 단순 알림 같은 작업은 DB 트랜잭션을 고려할 필요가 없다.
  - 이 경우 `ResourcelessTransactionManager` 옵션 고려할 수 있다.
- 일단, `new ResourcelessTransactionManager()` 로 때려박자. 자세한건 5장에서 배운다.
```angular2html
@Bean
public Step zombieCleanupStep() {
    return new StepBuilder("zombieCleanupStep", jobRepository)
            // Tasklet과 ResourcelessTransactionManager 설정
            .tasklet(zombieProcessCleanupTasklet(), new ResourcelessTransactionManager())
            .build();
}
```

### 태스크릿 지향 처리 정리
- 단순 작업(알림, 파일 복사, 오래된 데이터 삭제)에 사용하는 Step 유형
- `tasklet.execute()` 메서드는 `RepeatStatus` 를 반환하며, 실행 반복 여부를 결정한다.
- Spring Batch 는 `tasklet.execute()` 실행 전후로 트랜잭션을 시작하고 커밋 -> DB의 일관성과 원자성 보장./
