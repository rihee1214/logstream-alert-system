# 아키텍처 변경 이력 기록

## V0.1
1. 초기 아키텍처 구성 완성

## V0.2
1. Alertmanager 구성 추가
    - Notification Service와 Alertmanager가 하는 역할이 다름을 인지.
    - Notification Service : 문제 발생시, 상세 내역을 특정 인물들에게 전송하기 위함.
    - Alertmanger : 시스템 문제 발생시, 그 즉시 그 내용을 알림
    - **결론 :** 긴급성과, 역할의 차이로, Alertmanager에 대한 구성을 추가함

## V0.3 (2025-04-22)
1. 로그 전송용 Agent제거 
    - Prometheus가 Actuator를 호출하여 Alertmanager로 바로 보냄
    - 발생한 시스템 로그는 Filebeat가 전송
    - 복잡한 처리 로직 필요 없음. 공통 부분만 module로 관리하여 신경쓰고, 나머지는 그대로 기록.
    - **결론 :** Kafka 전송만을 위한 Agent 구성은 불필요

## V0.35 (2025-04-23)
1. 모니터링 아키텍처 수정
   - 서비스 간 호출 흐름을 추적할 수 있도록 Zipkin 추가
   - 로그/지표/트레이스를 통합 관리할 수 있도록 Monitoring Service 구성 추가
   - Grafana, Kibana, Zipkin을 Monitoring Service 하위로 시각적으로 통합
   - **결론 :** 로그, 지표, 트레이스 데이터를 하나의 흐름으로 파악할 수 있도록 모니터링 계층 정비 및 집약