# Spring Batch Listener

### 성능과 모범 사례
1. 적절한 리스터 선택하기
- JobExecutionListener: 전체 작전의 시작과 종료를 통제
- StepExecutionListener: 각 작전 단계의 실행을 감시
- ChunkListener: 시스템을 청크단위로 제거할 때, 반복의 시작과 종료 시점을 통제
- Item[Read|Process|Write]Listener: 개별 아이템 식별 통제

2. 예외 처리
- listener의 before 에서 예외가 발생하면 Job/Step 이 실패한 것으로 간주된다. -> 예외처리 적절히

3. 실행 빈도 고려하기
- Job/StepExecutionListener
  - Job/Step 실행 당 한 번씩 실행되므로 비교적 안전하다
- ItemRead/ProcessListener
  - 매 아이템마다 실행되므로 조심해야 한다.
```angular2html
@Override
public void afterRead(Object item) {
    heavyOperation(); // 외부 API 호출 등
}
```

4. 리소스 사용을 최소화: 항상 실행 빈도와 리소스 사용을 고려하여 신중하게 사용하기.
- DB 연결, 파일I/O, 외부 API 호출은 최소화
- 리스너 내 로직은 가능한 가볍게
  - 특히 Item 단위 리스너는 더욱 중요

### JobExecutionListener
- Job 실행 시작과 종료에 호출
- `afterJob()`은 잡 실행 정보가 메타데이터 저장소에 저장되기 전 호출.
  - 이를 활용해 특정 조건에 따라 Job의 실행 결과 상태를 완료(COMPLETED)에서 실패(FAILED)로 변경하거나 그 반대도 가능

### StepExecutionListener
- Step 실행 시작과 종료에 호출
  - `afterStep()`의 ExitStatus 는 일단 넘어가자

### ChunkListener
- 하나의 청크가 처리되기 시작 전, 완료 후, 에러 발생했을 때 호출됨.
  - `afterChunk`는 트랜잭션이 커밋된 후 호출.
  - `afterChunkError`는 트랜잭션이 롤백된 이후 호출

### Item[Read|Process|Writer]Listener
- 아이템 읽기,처리,쓰기의 처리 전후와 에러 발생 시 호출됨
  - ItemReadListener.afterRead() 는 ItemReader.read() 가 읽을 게 없어 null 을 반환하면 호출되지 않는다.
  - ItemProcessListener.afterProcess()는 ItemProcessor.process() 가 null 을 반환해도 호출된다.
    - 처리의 null 은 해당 데이터를 필터링 하겠다는 의미 -> 4장에서 보충
  - ItemWriterListener.afterWriter()는 트랜잭션이 커밋되기 전, ChunkListener.afterChunk()가 호출되기 전에 호출.

### 용도는?
- 단계별 모니터링과 추적: Job과 Step의 실행 전후에 로그 남김 -> 배치 작업의 모든 단계를 모니터링 가능.
- 실행 결과에 따른 후속 처리: Job과 Step의 실행 상태에 따라 조치 가능.
- 데이터 가공/전달: 실제 처리 로직 전후에 데이터 추가/정제 가능.
- 부가 기능 분리
