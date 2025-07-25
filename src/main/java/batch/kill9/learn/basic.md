#### Spring Batch 기본 
- Spring Batch 가 제공하는 영역과 개발자가 제어하는 영역으로 나눈다.
- Job 은 여러 개의 Step 으로 구성

### Spring Batch 가 제공하는 영역
- Spring Batch 는 배치 잡을 만들기 위해 필요한 대부분의 컴포넌틑와 실행 흐름을 제공한다.

- Job & Step: Spring Batch가 제공하는 핵심 컴포넌트
- JobLauncher : Job을 실행하고 실행에 필요한 파라미터를 전달하는 역할. 배치 작업 실행의 시작점
- JobRepository : 배치 처리의 모든 메타데이터를 저장하고 관리하는 핵심 저장소로 Job과 Step의 실행 정보(시작/종료시간, 상태, 결과)를 기록
  - 재시도나 복원에 사용
- ExecutionContext : Job과 Step 실행 중의 상태 정보를 key-value 형태로 담는다.
  - Job 과 Step 간의 데이터 공유나 Job 재시작 시 상태 복원에 사용
- 데이터 처리 컴포넌트 구현체: 데이터를 '읽기-처리-쓰기' 방식으로 처리하며 다양한 구현체 제공
  - ItemReader 구현체 : JdbcCursorItemReader, JpaPagingItemReader, MongoCursorItemReader 등 다양한 데이터 소스로부터 데이터를 읽어옴
  - ItemWriter 구현체 : JdbcBatchItemWriter, JpaPagingItemWriter, MongoItemWriter 등을 통해 처리된 데이터를 저장

### 개발자가 제어해야 하는 영역
- Job & Step 구성
  - `@Configuration`을 사용해 Job과 Step의 실행 흐름을 정의한다.
  - 각 Step의 실행 순서와 조건을 설정하고, Spring 컨테이너에 등록해 Job의 동작을 구성
  - Spring DI로 ItemReader, ItemProcessor, ItemWriter 등 배치 작업에 필요한 컴포넌트들을 조합하고 배치 플로우 완성
- 데이터 처리 컴포넌트 활용
  - CSV의 각 컬럼을 자바 객체의 프로퍼티와 매핑하는 방식은 개발자가 직접 지정.
- 단순 작업 처리
  - 모든 배치 잡이 **읽고-처리-쓰기** 는 아니다. 때로는 파일 복사, 디렉토리 정리, 알림 발송과 같은 작업이 필요하다.
- 과거 Redis 컴포넌트는 Batch 5.1 부터 등장한 것처럼, Batch가 지원하지 않는 데이터 소스는 직접 구현해야 한다.
- 