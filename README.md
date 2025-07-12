# Getting Started

### Spring Batch
- 스프링 배치는 스프링의 장점인 DI, AOP, 추상화 계층을 그대로 계승한다.

### 표준 방식
- Job: 하나의 완전한 배치 처리 작업
- Step: Job의 세부 실행 단계(하나의 Job은 여러 Step으로 구성 가능)
- ItemReader: 데이터를 읽어오는 컴포넌트
- ItemProcessor: 데이터를 가공하는 컴포넌트
- ItemWriter: 데이터를 저장하는 컴포넌트

### 배치 처리의 핵심과 특징
- ItemReader와 ItemWriter라는 표준화된 인터페이스를 통해 어떤 데이터 소스든 일관된 방식으로 처리가능.

#### 지원하는 데이터 소스
- 플랫 파일(CSV, TXT): FlatFileItemReader, FlatFileItemWriter 를 사용해 간단 처리
- XML/JSON: StaxEventItemReader, JsonItemReader, JsonFileItemWriter 등을 사용한 구조화된 데이터 처리
- RDB: JdbcCursorItemReader, JdbcBatchItemWriter 등으로 DB접근, JPA 같은 ORM도 지원
- NoSQL: 몽고, 레디스를 위한 전용 Reader/Writer 제공
- 메시징 시스템: Kafka, AMQP 등 메시지 큐 시스템과 연동 지원

#### 트랜잭션 지원
- 체크포인트 기능
  - 긴 배치 작업 중간에 안전 지점 설정
  - 문제 발생 시, 안전 지점(체크포인트)부터 시작 가능
- 유연한 트랜잭션 범위 설정
  - 처리 데이터 양/특성에 따라 트랜잭션 범위 조절 가능
  - 작은 단위의 커밋으로 메모리 사용량 조절 가능

#### 재시작 기능과 실행 제어
- 재시작 기능: 대용량 데이터 처리 중 실패해도 처음부터 다시 시작할 필요 없다.
  - 오류 발생 시, 마지막 성공한 Step부터 재시작 가능
  - Step 내에서도 마지막 처리 항목 이후부터 작업 재개 가능
- 유연한 실행 제어
  - 특정 Step만 선택적으로 실행 가능
  - 파라미터를 통한 동적 작업 제어
  - Step의 조건부 실행: 이전 Step의 결과에 따라 다음 작업 결정 가능
- 작업 상태 추적
  - 모든 Job과 Step의 실행 상태를 메타데이터로 관리
  - 작업의 실행 시점과 결과를 정확하게 추적 가능

#### 대용량 데이터 처리
- Chunk 지향 처리
  - 대량 데이터를 청크로 나누어 순차적 처리
  - 한 번에 처리하는 데이터 양을 제한하여 메모리 사용 최적화
- 효율적인 데이터 읽기
  - 페이징 방식: 일정 크기만큼 데이터 조회후 처리
  - 커서 방식: DB 커서를 사용한 효율적인 데이터 스트리밍
- 확장성
  - 멀티 스레드 Step: 하나의 Step을 여러 스레드로 처리
  - 병렬 Step: 여러 Step을 동시에 실행
  - 분산 처리: 여러 서버에서 배치 처리를 분산 실행.

