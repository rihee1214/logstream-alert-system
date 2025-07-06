# 로그 테이블 PK 전략

## 로그 스키마

| 스키마 명                  | 스키마 설명                                                    |
| ---------------------- | --------------------------------------------------------- |
| logtype                | 로그 유형 (biz, sys, act)                                     |
| timestamp              | ISO 8601 DateTime (with offset)                           |
| **uuid**               | 로그 식별자(중복 가능성 높으므로 traceId, timestamp, service등과 조합하여 사용) |
| **log_schema_version** | 로그 버전 (로그 스키마 변경시 버전 1추가)                                 |
| level                  | 로그 레벨 (INFO, WARN, ERROR)                                 |
| service                | 서비스 식별자                                                   |
| class                  | 로그 발생 클래스 (FQCN)                                          |
| message                | 로그 메시지 본문                                                 |
| host                   | 로그 기록 서버의 호스트명                                            |
| container              | 컨테이너 ID                                                   |
| stacktrace             | 예외 발생 시 출력되는 스택 트레이스                                      |
| traceId                | 전체 요청 흐름의 식별자                                             |
| spanId                 | 개별 작업 단위 식별자                                              |
| parentSpanId           | 상위 스팬 ID                                                  |
| sampled                | 트레이싱 여부 (1 or 0)                                          |
| flags                  | 디버깅 플래그 (1 or 0)                                          |
| call_type              | 어떤 방식으로 Call했는지 (http)                                    |
| call_method            | HTTP 메서드 (GET, POST 등)                                    |
| call_uri               | 요청 URI                                                    |
| call_statusCode        | 응답 상태 코드 (예: 200)                                         |
| call_statusMessage     | 응답 상태 메시지 (예: OK)                                         |
| call_elapsedMs         | 요청-응답 간 소요 시간 (ms)                                        |
| call_remoteTraceId     | 상대가 사용하는 TraceId                                          |
- UUID항목을 추가해서 MV에서 정확한 로그를 지정하여 base table에서 데이터를 가져올 수 있도록 한다.
- 결국 timestamp, service, traceId, spanId, uuid 이 다섯가지 항목으로 조회할 수 있도록 한다.
- 클러스터링 전략은 timestamp, service  나머지는 파티셔닝 조건으로 하여 모든 PK조건(클러스터링 + 파티셔닝 키) 조건을 만족시킨다.
- UUID는 로그를 찍을 때 생성하지 말고, insert 시에 생성하게 한다. (biz service의 부하를 줄이고, 필요한 곳에서 생성하도록 생성 주체를 변경한다.)
- MV테이블은 목적에 따라 가져오는 스키마가 다르겠지만 보통 위의 다섯가지 항목을 가져와서 base table에 조회할 수 있도록 한다.
- PK정리
	- **Clustering Key**
	    - `service`, `timestamp`
	    - 로그 분석 시, **특정 서비스에서 특정 시간대에 발생한 로그를 시간 순으로 정렬해 조회**하는 경우가 많기 때문에 이 순서로 구성
	    - 클러스터 내부에서는 `service` 단위로 정렬된 후, 시간순으로 빠르게 scan 가능
	- **Partitioning Key**
	    - `traceId`, `spanId`, `uuid`
	    - **추적 기반 분석(traceId → spanId → uuid)** 을 가능하게 하기 위한 구성
	    - 대부분의 쿼리에서 전체 PK 조건을 명시할 예정이므로, 파티션 키를 점차 좁혀가며 명확한 단일 로그 식별 가능
	    - 일반적으로 `traceId`가 가장 범위가 넓고, `spanId`, `uuid` 순으로 좁아질 가능성이 높아 이 순서가 적절함

---
## 로그 스키마 추가 전략

## ✅ 로그 스키마 확장 시 고려해야 할 운영 전략

### 1. **Schema Evolution 관리 원칙 수립**

- 스키마는 **추가만 허용**하고, **삭제 또는 필수화 금지**
    - ex) 새로운 필드는 선택적(Optional)이어야 함
    - 과거 데이터는 해당 필드를 `null`로 간주
- JSON 기반 storage일 경우엔 단순하지만, **정형화된 Column 기반**이면 MV 설계와 데이터 정합성 이슈 발생

> 🔹 **ScyllaDB**나 **Cassandra**에서는 스키마 변경이 가능하지만, MV에는 자동 전파되지 않음  
> ➜ MV를 recreate하거나, 새로운 MV를 따로 만들어야 함

---

### 2. **버전 명시 전략**

- 모든 로그에 **`log_schema_version`** 필드를 추가하고,    
    - ex) `v1`, `v2.1`, `v3` 등
- 쿼리 혹은 분석 시 **버전에 따라 해석 또는 필터링 처리** 가능하도록 구성
    - ex) Alert Rule에서 `version = v2` 이상일 때만 필드 필터링
- 반드시 로그 저장 application(loggin service)에서 버저닝을 넣도록 할 것

---

### 3. **MV 구성 기준 변경 전략**

- 기존 필드 기반 MV는 유지,    
- 새 필드 기반 조회가 필요하면 **별도 MV 구성**
    - ex) `userId`, `call_target`으로 필터링하고 싶을 경우 `v2_mv_user_calltarget` 생성

---

### 4. **분석 도구/대시보드와의 연계 전략**

- Grafana, Kibana 등의 대시보드는 **선택적으로 필드 존재 여부 판단 로직** 가능
    - ex) `field exists` 조건 사용        
- 추후 Elastic이나 Lakehouse 계열로 확장 시, **`flatten + null-safe` 처리 기준 사전 정의**

---

### 5. **로그 수집/처리 파이프라인의 유연화**

- Fluent Bit / Logstash / Logging Service 내에
    - 필드 존재 유무에 따른 **표준화(normalization) 처리 로직** 삽입        
- 모든 로그는 내부적으로 **정규화된 JSON 포맷**으로 구조화되어야 향후 변경 대응이 쉬움

---

## 🔄 결론

스키마 추가는 자연스럽고 필요하지만,  
**"과거 로그와 함께 어떻게 해석될 것인가?"**, **"분석 시스템과 알림 시스템은 어떻게 달라질 것인가?"**  
이런 관점에서 다음 4가지는 반드시 고려되어야 합니다:

1. **버전 명시 + 선택적 필드**
2. **분석 전략 분기 (MV 및 대시보드 측)**    
3. **스키마 관리 정책 수립 문서화**
4. 버전 관리를 위한 **백 필(Backfill) 전략**

**실제 운영중 백필 전략 과 버전관리에 대한 전략 고려 필요**

버저닝을 추가하여 어떻게 분석할지 고민하는 전략.
버저닝을 추가하지 않고 백필을 진행하는 전략
- **새로운 로그 스키마를 반영한 새로운 도커 이미지**를 제작
- 카나리아 배포 방식으로 일부 인스턴스만 먼저 교체
- 모든 인스턴스가 새 이미지로 교체되면 → 관련 consumer 및 downstream 로직 전환
- 동시에, 별도 도커 백필 컨테이너를 띄워 과거 로그를 새로운 스키마로 변환
- 백필 중에는 **과거 로그 분석/조회 기능을 잠시 비활성화**하거나 degrade
- 백필 완료 후 → 전체 시스템 통합 전환
## 주의사항
1. PK의 조회시 주의해야 하는 사항
	1. Partitioning key : 모든 key가 where 절에 있어야 함
	2. clustering key : 아예 없거나 일부만 있어도 됨

---
# 로그 분석 전략

## 분석 흐름 구성

1. **사전 정의된 분석 쿼리 요구사항 확인**
	- 사용 목적(대시보드, 알림, 보고서)에 따라 쿼리 설계
2. **MV(Materialized View) 정의**
	- 주 조회 필드를 기반으로 MV 설계 (ex. service, status, timestamp)
	- TTL은 7~14일 내외로 설정하여 단기 분석 최적화
3. **MV → BaseTable 연계**
	- MV로 파티션 필터링 수행
	- MV에 저장되지 않은 전체 필드는 BaseTable에서 조회 (join 또는 재요청)
4. **분석 애플리케이션 처리**
	- 분석 로직은 서비스 단에서 수행
	- 버전 불일치 또는 필드 누락은 application 단에서 필터링 또는 fallback

## 운영 환경 고려사항

### MV 생성 및 전파
- Scylla는 Gossip Protocol을 사용하므로, 한 노드에서 MV 생성 시 클러스터 전체에 전파됨
- 단, 전파는 eventual consistency이며 schema agreement가 필요

### 배포 및 자동화
- MV 추가는 스키마 변경이므로 CI/CD에 포함하기 어려움
- Helm Chart 또는 Custom Init Container에서 MV 포함된 schema 파일 적용 권장

### 스키마 변경 전략
- 기존 스키마에 영향 없이 MV만 추가하는 경우 → 개별 노드 Rolling Update 가능
- 스키마 자체를 변경하는 경우 → downtime 또는 미러 클러스터 통한 migration 고려
- 스키마 변경 내역은 GitOps + DR 문서로 기록 필요
