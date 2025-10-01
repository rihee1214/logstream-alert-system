# 📘 logging.properties 설명서

> 본 문서는 **logging-service 모듈 전용 설정**을 정의합니다.
> 
> 로그 생성의 책임은 `biz` 모듈에 있으며, 그 로그를 수집 받아 영속성 저장소에 저장하는 역할을 맡고 있습니다.
> 자세한 구성 및 동작은 [logging-service 개발 가이드](../../../development/logging-service/logging-service.md)을 참고하세요.

---

# 🧾 공통 정보

### 🔧 `worker.processors`
- **타입** : `String`
- **예시 값** : `collector,validator,persistence`
- **설명**
    - 파이프라인 동작의 순서와 유형을 지정합니다.
    - `,` 로 구분하며, 지정한 순서대로 실행됩니다.

### 🔧 `${processorName}.type`
- **타입** : `String`
- **예시 값** :
    - `collector.type=kafka`
    - `validator.type=default`
    - `persistence.type=postgres`
- **설명**
    - 각 파이프라인 단계에서 사용할 **구현체**를 지정합니다.
    - 반드시 `worker.processors`에 명시된 컴포넌트마다 하나씩 지정해야 합니다.

---
# 1. Collector 설정 정보

## 1. Kafka

### 예시
```
kafka.topic=log_topic  
kafka.fetch.ms=5000

kafka.consumer.bootstrap.servers=localhost:9092  
kafka.consumer.group.id=logging-worker  
kafka.consumer.max.poll.interval.ms=300000
kafka.consumer.enable.auto.commit=false  
kafka.consumer.auto.offset.reset=earliest  
kafka.consumer.max.poll.records=100  
kafka.consumer.key.deserializer=org.apache.kafka.common.serialization.StringDeserializer  
kafka.consumer.value.deserializer=org.apache.kafka.common.serialization.StringDeserializer
```
### 상세 설명

- **kafka.topic**
    - **타입** : `String`
    - **예시 값** : `log_topic`
    - **설명** : 구독할 토픽 이름. 여러 개 지정 시 `,` 로 구분 (공백 제거).
- **kafka.fetch.ms**
    - **타입** : `Integer (ms)`
    - **예시 값** : `5000`
    - **설명** : 앱 전용 `poll()` 호출 대기시간.
        - 데이터가 없으면 해당 시간까지 블로킹 대기 후 반환.
        - Kafka 클라이언트 설정과는 별도로, `KafkaConsumer.poll(Duration)` 인자로만 사용.
- **kafka.consumer.bootstrap.servers**
    - 브로커 주소 리스트 (`host:port`, 콤마 구분).
- **kafka.consumer.group.id**
    - 컨슈머 그룹 ID.
- **kafka.consumer.max.poll.interval.ms**
    - 두 `poll()` 호출 사이의 최대 허용 간격(ms).
    - 초과 시 리밸런스 발생 → 처리시간이 긴 경우 충분히 크게 잡아야 함 (기본 5분).
- **kafka.consumer.enable.auto.commit**
    - 자동 커밋 여부.
    - **false 권장** → **저장 성공 시점에 배치 단위로 수동 커밋**해야 일관성 보장.
- **kafka.consumer.auto.offset.reset**
    - 커밋 오프셋이 없거나 범위를 벗어난 경우 시작 위치.
        - `earliest`: 가장 오래된 위치부터 (신규 그룹/재처리에 적합)
        - `latest`: 가장 최신 위치부터 (과거 로그는 무시)
        - `none`: 조건 불충족 시 예외 발생
- **kafka.consumer.max.poll.records**
    - 한 번의 `poll()`에서 가져올 최대 레코드 수 (배치 크기).
- **kafka.consumer.key.deserializer / value.deserializer**
    - Kafka 메시지 키/값 역직렬화 클래스.

> ⚠️ **주의**
> - `poll()` 호출이 `max.poll.interval.ms`(기본 5분)를 초과하지 않도록 조정해야 합니다.
> - `enable.auto.commit=false` 시, 반드시 **저장 성공 후에만 수동 커밋**해야 중복/손실을 방지할 수 있습니다.

---
# 2. Validator 설정 정보
## 1. Default

- **추가 설정 없음**
- 단일 메시지/배치 메시지에 대해 기본 검증 로직만 수행합니다.**

---
# 3. Persistence 설정 정보
## 1. PostgreSQL

#### 예시
```
postgres.connect.url=jdbc:postgresql://localhost:5432/LogForDebugging?reWriteBatchedInserts=true
postgres.connect.username=username  
postgres.setting.maximum.pool.size=30  
postgres.setting.minimum.pool.size=2  
postgres.setting.idle.timeout=600000  
postgres.setting.connection.timeout=10000  
postgres.setting.max.lifetime=1800000
```
### 상세 설명

- **postgres.connect.url**
    - PostgreSQL JDBC URL.
    - `?reWriteBatchedInserts=true` 옵션 권장 → 배치 Insert 성능 최적화.
- **postgres.connect.username**
    - 접속 계정 ID.
    - 비밀번호는 환경 변수나 Secret Manager를 통해 주입.
- **postgres.setting.maximum.pool.size**
    - 커넥션 풀 최대 크기.
- **postgres.setting.minimum.pool.size**
    - 커넥션 풀 최소 크기.
- **postgres.setting.idle.timeout**
    - 풀에서 유휴 연결을 유지하는 최대 시간(ms).
- **postgres.setting.connection.timeout**
    - 커넥션을 풀에서 가져올 때 대기하는 최대 시간(ms).
- **postgres.setting.max.lifetime**
    - 커넥션의 최대 수명(ms).
    - DB/네트워크 타임아웃보다 약간 짧게 설정 권장.

---