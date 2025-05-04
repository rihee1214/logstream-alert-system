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