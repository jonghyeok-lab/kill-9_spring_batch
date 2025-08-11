# JobParameters

### 뭐가 좋음?
1. 동적
  - 웹 요청이 들어올 때마다 배치를 실행할 때 -> 요청으로 들어온 파라미터를 동적으로 배치 앱에 전달 가능하게 해준다.

2. 메타데이터
- Spring Batch 는 Job Parameters의 모든 값을 메타데이터 저장소에 기록.
  - Job 인스턴스 식별 및 재시작 처리
  - Job 실행 이력 추적

- 메타데이터?
  - Job/Step의 시작/종료시간, 실행 상태, 처리된 레코드 수 등이 포함됨

### JobParameters 전달하기
- 실제 운영 -> 커맨드 라인을 통해 파라미터 전달이 핵심
  - CI/CD(젠킨스 ...) 를 포함한 대부분의 스케줄러 도구들이 커맨드라인 실행을 기본으로 지원

#### 커맨드라인에서 JobParameters 전달하기
- 파일 말소 예제
  - ex) `./gradlew bootRun --args='--spring.batch.job.name=dataProcessingJob inputFilePath=/data/input/users.csv,java.lang.String'`
  - 잡 파라미터: `inputFilePath=/data/input/users.csv,java.lang.String`
  - 스프링부트 아규먼트: `--spring.batch.job.name=dataProcessingJob`

#### JobParameters 기본 표기법
- `parameterName=parameterValue,parameterType,identificationFlag`
  - `parameterName`: 배치 Job에서 파라미터 찾을 때 사용하는 key 값
  - `parameterValue`: 파라미터의 실제 값
  - `parameterType`: fully qualified name 으로 파라미터 타입 사용.명시하지 않으면, Batch 는 String으로 가정
  - `identificationFlag`:
    - Batch가 해당 파라미터가 JobInstance 식별에 사용될 파라미터 여부인지?
    - 생략할 경우 true 값을 가지며, true 면 식별에 사용된다는 의미
    - JobInstance 는 5장에서 다룬다.

- commandLine 명령어 분석
```angular2html
jobPath <options> jobIdentifier (jobParameters)*
./gradlew run --args="com.system.batch.DataProcessingJobConfig dataProcessingJob inputFilePath=/data/input/users.csv,java.lang.String userCount=5,java.lang.Integer,false

- jobPath: com.system.batch.DataProcessingJobConfig
- <options>: 생략됨
- jobIdentifier: dataProcessingJob
- jobParameters: inputFilePath=/data/input/users.csv,java.lang.String userCount=5,java.lang.Integer,false
```

#### DefaultJobParametersConverter (parameterType의 변환기)
- parameterType 에 올 수 있는 타입은 DefaultJobParametersConverter 가 Spring의 DefaultConversionService를 사용한다.
  - 그래서, DefaultConversionService 가 제공하는 다양한 타입의 변환을 지원

- LocalDate/Time 은 ISO 기준으로 하면 된다. Enum도 OK
- netsted class 는 TerminatorConfig$QuestDifficulty 처럼 `$`로 구분
- POJO도 된다. -> ./gradlew 명령어는 POJO의 필드를 넣는다.
- `,` 를 넣어야 할 경우, JSON으로 표기
  - `./gradlew bootRun --args='spring.batch.job.name=terminatorJob infilter={}'`

#### JobLauncherApplicationRunner
- Spring Boot가 제공하는 ApplicationRunner로 커맨드라인으로 전달된 잡 파라미터를 해석하고, 실제 Job을 실행한다.

#### JobExecution
- Batch의 Job 실행 정보를 갖고 있는 객체
  - Job 실행 시점에 생성되어 실행 상태, JobParameters, 실행 결과를 포함. 
- **JobParameters 에 접근 가능**하다. 