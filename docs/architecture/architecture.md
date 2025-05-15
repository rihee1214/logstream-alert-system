# 아키텍처 변경 이력 기록
---
## V0.1
1. 초기 아키텍처 구성 완성
---
## V0.2
1. Alertmanager 구성 추가
    - Notification Service와 Alertmanager가 하는 역할이 다름을 인지.
    - Notification Service : 문제 발생시, 상세 내역을 특정 인물들에게 전송하기 위함.
    - Alertmanger : 시스템 문제 발생시, 그 즉시 그 내용을 알림
    - **결론 :** 긴급성과, 역할의 차이로, Alertmanager에 대한 구성을 추가함
---
## V0.3 (2025-04-22)
1. 로그 전송용 Agent제거 
    - Prometheus가 Actuator를 호출하여 Alertmanager로 바로 보냄
    - 발생한 시스템 로그는 Filebeat가 전송
    - 복잡한 처리 로직 필요 없음. 공통 부분만 module로 관리하여 신경쓰고, 나머지는 그대로 기록.
    - **결론 :** Kafka 전송만을 위한 Agent 구성은 불필요
---
## V0.35 (2025-04-23)
1. 모니터링 아키텍처 수정
   - 서비스 간 호출 흐름을 추적할 수 있도록 Zipkin 추가
   - 로그/지표/트레이스를 통합 관리할 수 있도록 Monitoring Service 구성 추가
   - Grafana, Kibana, Zipkin을 Monitoring Service 하위로 시각적으로 통합
   - **결론 :** 로그, 지표, 트레이스 데이터를 하나의 흐름으로 파악할 수 있도록 모니터링 계층 정비 및 집약
---
## V0.4 (2025-04-25)
1. 중복된 서비스 제공
   - 로그 정책의 변경에 의해 system logging service를 logging service에 통합
     - 대신 mock service에서 로그 생성시 logtype을 담아서 보내도록 정책 수립
2. 로그 저장 인덱스 형식 변경
   - 기존에는 모든 로그를 타입별로 하나의 인덱스를 사용
     - 대신 맨 뒤에 YYYY.MM.dd 를 넣어 날짜별로 로그를 저장하도록 인덱스 설정
       - before) biz-log-index
       - after)  biz-log-2025.04.25
   - 로그를 분산시켜서 빠른 탐색이 되도록하고, 정리하기도 편한 방식
   - 모니터링 프로그램(zipkin, grafana, kibana)이 분석가능 함
     - 인덱스가 날짜별로 나뉘었기 때문에, Kibana index pattern(biz-log-*)으로 쉽게 분석 가능
     - Zipkin traceId 기반 분산 추적에 전혀 영향을 주지 않음
3. 중복되는 큐 제거
   - 기존에는 mock service에서 각기 다른 서비스로 로그를 보내서 큐를 두개 사용
     - 서비스가 하나가 되었고, logtype이 log에 들어있기에 큐 하나만 사용
   - notification 서비스 들에게 보낼때도 적용.
     - 큐를 하나만 쓰도록하여, 유사한 기능을 하는 서비스는 하나의 큐를 사용하도록 지정
---
## V0.41 (2025-04-26)
1. Filebeat 제외
   - Spring logAppender중 Kafka Appender를 활용하기로 결정
   - 기존 구성도 내 Filebeat 관련 설정 수정
2. 문서 현행화
   - 아키텍처 구성도 및 관련 문서 업데이트
---
## V0.45 (2025-05-04)
1. Filebeat 재 도입 및 KafkaAppneder 제거
	- KafkaAppender는 사용하지 않기로 결정
		- Spring 공식 지원 appender가 아님
		- 업데이트가 중단되어 **보안상 문제 발생 가능성** 높음
		- 유지보수 중단 상태로 **구성도 및 문서에서 제거**
	- Logging 구조에서 Filebeat 방식으로 전환
		- Mock 서비스 컨테이너 내에만 설치 (핵심 서비스 로그만 수집)
		- Logging Service의 부하 분산 목적
2. Log Index 구성 변경
	- 기존 인덱스: `biz-log-YYYY.MM.dd`, `sys-log-YYYY.MM.dd`
	- 신규 인덱스 추가: `act-log-YYYY.MM.dd`
	    - `act`는 actuator 로그로서 **metric 및 health 정보 전용**
	    - `sys`와 기능이 명확히 분리됨
3. 문서 현행화
	- 아키텍처 구성도 수정 (Filebeat 및 logtype 분기 포함)
	- 관련 decision 문서 보완 및 정리
---
## V0.5 (2025-05-15)
1. Prometheus Wrapping Service 도입
	- 기존 Prometheus 단독 구조의 한계를 보완하고, 정책 유연성과 장애 대응 능력을 강화하기 위해 Wrapping Service를 도입하였다.
		1. Alertmanager 전송 실패에 대한 Failover 전략 미비
		2. Elasticsearch 전송 미지원 (별도 remote_write adapter 필요)
		3. 각 BIZ 서비스별 정책 적용/변경의 어려움
	- Wrapping Service는 Prometheus API를 통해 데이터를 수집하고, 이를 기반으로 **ElasticSearch 저장, Alert 전송, 정책 적용** 등을 책임진다.
2. Metric 전용 Log Index 추가
	- 기존에는 health 정보와 metric 정보가 혼합되어 있었지만, **분석 및 관리 효율성을 위해 분리된 인덱스를 도입**하였다. (`metric-log-YYYY.MM.dd` 인덱스 신설)
		1. `act-log`와 분리됨으로써 리소스 소비 감소
		2. 메트릭 분석 시 시계열 전용 쿼리 최적화 가능
		3. 구조적으로 Prometheus 흐름과 Biz Health 흐름을 명확히 구분
3. Prometheus 장애 및 알림 정책 (Wrapping 기준)
	- Wrapping Service는 Prometheus의 상태와 알림 전송 실패를 감지하고 다음과 같이 처리한다
		1. Prometheus 응답 실패를 감지하며, **5회 연속 실패 시 Alertmanager에 장애 알림 전송**
		2. Wrapping Service는 Prometheus로부터 메트릭을 수집하여 **ElasticSearch에 저장하는 책임을 가짐**
		3. 다음과 같은 조건 충족 시 Notification Service를 통해 Fallback 알림 전송:
			   - Alertmanager 전송 실패 5회 이상
			   - Elasticsearch 전송 실패 5회 이상
			   - Alertmanager 또는 Elasticsearch 전송 실패율이 1분 동안 20% 초과
4. Prometheus 및 Wrapping Service의 HA 구성
	- Prometheus는 서비스 메트릭을 scrape하며, 고가용성(HA)을 위해 여러 인스턴스로 분산 구성될 수 있다.
	- Prometheus는 A-A(Active-Active) 구조로 동작하며, 모든 인스턴스가 동일한 메트릭 대상에 대해 독립적으로 scrape을 수행한다.
	- Wrapping Service는 Prometheus의 메트릭 API를 통해 수집된 데이터를 처리하는 수동 컴포넌트이며, HA 구성이나 리더 선출에 관여하지 않는다.
	- 각 Wrapping 인스턴스는 **자신이 담당하는 Prometheus 인스턴스의 실패만 감지하고 대응**하며,
	  다른 Prometheus나 Wrapping 인스턴스의 상태에는 영향을 받지 않는다.
	- 수집된 메트릭 데이터는 Elasticsearch에 저장되며,
	  저장 중복을 방지하기 위해 `_id` 기반 중복 제어 메커니즘을 사용하여 저장 일관성을 보장한다.
	  `_id`는 메트릭의 timestamp, metric name, labels 등을 조합하여 생성된 해시값이다.
	- Wrapping Service는 동일한 장애 상황에서 Alertmanager 및 Notification Service로 중복 알림이 전송되지 않도록, 모든 알림 payload에 `dedup_key` 라벨을 포함하여 전송한다.
	- Alertmanager 및 Notification Service는 이 키를 기준으로 일정 시간 동안 중복 알림을 억제하며,장애 알림이 한 번만 전송되도록 보장한다.
	- Prometheus의 장애 대응 및 데이터 신뢰성 확보는 **Prometheus 자체의 HA 구성을 통해 보장**되어야 하며, Wrapping Service는 수집된 데이터를 후처리하는 데만 집중한다.