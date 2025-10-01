# 0. Logging Service 전체 구성도

![LoggingService_Architecture](./images/LoggingService구성도.drawio.svg)
# 1) 개요

- **목적**: 로그를 수집→검증→저장하는 파이프 라인.
- **실행 흐름**: 초기화(설정 로딩/플러그인 조립) → 런타임(Worker 루프) → 종료(자원 해제).

# 2) 런타임 순서(Worker 루프)

```
1) Config가 만들어낸 List<LogProcessor> 주입받음
2) List<LogProcessor>중에서 Committable요소 확인
while (true):
	1) Collector에서 메시지를 묶음으로 수집
	2) Validator/Persistence 순서로 LogProcessorPort 체인 처리
	3) 마지막에 "모든 committable에 대해" commit() 호출 → 사이클 종료
```
- **에러 처리**: 검증 실패/예외는 `LogErrorMessage`로 전환 후 같은 체인 타게 함.    

# 3) 커밋/일관성 모델(핵심만)

- **모델**: _at-least-once_ — 저장 성공 후에만 Source(예: Kafka) offset commit.
- **순서**: `Persistence → Collector.commit()` (역전 금지).
- **멱등성**: Persistence 영역에서 messageId를 통해 conflict가 발생할 경우 무시하도록 한다.

# 4) 교체/확장 포인트(어떻게 바꾸나)

## 1. 파이프라인 구성

- **교체 가능한 부품**: Collector / Validator / Persistence 
- 설정으로 어떤 구현체를 쓸지 지정 :

```
worker.processors=collector,validator,persistence

collector.type=kafka
validator.type=default
persistence.type=postgres
```
- **processorname.type** : 
  해당 파이프라인 구현체 클래스의 **Annotation 값**으로 어떤 구현체를 선택할지 결정합니다.
## 2. 기존 타입의 새로운 구현체 추가

1. 구현체 클래스를 해당 패키지에 추가
    - 예: `collector` → `com.rihee.alerting.loggingService.adapter.in.collector`    
2. 맞는 Annotation 부착
   ```
    @CollectorType("kafka") 
    public final class KafkaCollectorAdapter extends LogCollectorPort { ... }
    ```
3. `type` 값은 고유하게 부여 (중복 시 annotation processor가 오류 발생)

## 3. 새로운 프로세서 타입 추가 방법
1. **Port 정의**: `pipeline.port` 하위 적절한 위치에 `abstract class` 생성
	- `in` : 외부 입력 수집기    
    - `rule`: 데이터 보강/검증기
    - `out` : 저장/출력기
2. **Annotation & Processor 추가**:
    - annotation-processor 모듈에 신규 타입용 Annotation/Processor 구현
3. **Plugin Factory 추가**:
    - `plugin` 패키지에 설정 기반 인스턴스 생성 클래스 작성    
4. **Planner 확장**:
    - `LogProcessorPluginPlanner` enum에 신규 plugin을 매핑
5. 구현체 추가는 [2. 기존 타입 구현체 추가] 절차와 동일
