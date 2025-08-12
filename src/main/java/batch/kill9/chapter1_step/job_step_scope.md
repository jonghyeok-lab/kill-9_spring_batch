# Job과 Step의 Scope 이해하기
- JobScope와 StepScope 로 선언된 빈의 생명주기
  1. 프록시 객체로 존재
  2. Job/Step 이 실행된 후 실제 빈 생성
  3. Job/Step 종료할 때 함께 소멸

- 그래서, REST API로 의해 실행되는 Job에 @JobScope 가 있다면
  하나의 Job은 서로 다른 JobExecution 을 갖게되어 병렬로 처리되어 질 수 있다.
- @StepScope 도 마찬가지. Step과 함께 생명주기

### 주의사항
- 프록시 객체를 만들어야 하니 상속 가능해야 함.
- Step 빈 에는 Step/JobScope X

### 지연 바인딩
- `systemDestruction` 이란 Step 이 JobParameter 로 파라미터를 하나 받아야 하지만 Step 빈에는 Job/StepScope를 사용하지 않아 컴파일 에러가 발생한다.
  - 이럴 때, null을 주면 실제 값은 Job이 실행될 때 입력받은 JobParameters의 값으로 주입된다. -> 지연 바인딩 특성
```angular2html
@Bean
public Job systemTerminartJob() {
    return new JobBuilder("systemTerminationJob", jobRepository)
            .start(systemDestruction(null))
            .build();
```

### ExecutionContext
- JobExecution, StepExecution을 사용해 시작/종료 시간, 실행 상태같은 메타데이터를 관리
- 비즈니스 로직 처리중 발생하는 커스텀 데이터 관리 -> ExecutionContext 라는 데이터 컨테이너 사용
---
- Step의 ExecutionContext 에 저장된 데이터는 `#{jobExecutionContext['key']}` 로 접근 불가능
  - Step 수준의 데이터를 Job 수준에서 가져올 수 없다.
- 한 Step의 ExecutionContext 는 다른 Step에서 접근 불가능
  - 예를 들어, StepA의 ExecutionContext 에 저장된 데이터를 StepB 에서 `#{stepExecutionContext['key']}` 로 가져올 수 없다.
---
- Step간의 데이터 독립성을 완벽하게 보장하기 위해서.
- 

