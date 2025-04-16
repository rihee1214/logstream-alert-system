# 🚨 Distributed Event-Driven Alerting System

> Private MSA project for scalable, observable alerting system in distributed environments  
> Kafka 기반 비동기 이벤트 시스템으로 로그 수집 및 알림 전송을 담당하는 분산 아키텍처 프로젝트

---

## 📌 프로젝트 목적

본 프로젝트는 다양한 마이크로서비스에서 발생하는 로그를 Kafka 기반으로 수집하고,  
로그 레벨에 따라 알림을 비동기로 전송하며,  
장애 상황에 대한 복구 및 상태 모니터링까지 포함한 **확장 가능한 통합 알림 시스템**을 구축합니다.

- 모든 서비스의 로그를 일관된 방식으로 수집  
- 경고 이상 레벨 로그에 대한 비동기 알림 (SMS, Email) 전송  
- 전송 실패에 대한 재시도 및 실패 이력 관리  
- 로그 저장 및 분석 (Elasticsearch, Kibana, Grafana)  
- 시스템 상태 감시 (Prometheus, Alertmanager)

---

## 🛠 기술 스택

| 영역              | 사용 기술                              |
|------------------|-------------------------------------|
| Language         | Java 21                             |
| Framework        | Spring Boot 3.4.4                   |
| Messaging        | Apache Kafka                        |
| Database         | PostgreSQL, Redis                   |
| Log Storage      | Elasticsearch                       |
| Log Shipping     | Filebeat (EFK 구성)                  |
| Monitoring       | Prometheus, Grafana, Alertmanager   |
| Build Tool       | Gradle (Groovy DSL)                 |
| Deployment       | Docker, Docker Compose              |
| CI/CD            | GitHub Actions (예정)                |

---

## 🏗 아키텍처 구성도

┌──────────────┐     * Prometheus로 System Log를 수집하도록 처리 *
│ Mock Service │     * PostgreSQL을 제외한 모든 요소들은 Filebeat를 가지고 있음(EFK조합) * 
└─────┬────────┘     * Alertmanager로 System Log 문제를 Slack에 올리도록 처리함 * 
REST이용한 Log 전송           ┌──────────────────────┐                                      
┌─────▼─────┐              │ Kibana(로그 상세 검색용) ◀─────────────────┐
│   Agent   │              └──────────────────────┘   AllSystemLog  │
└─────┬─────┘              ┌───────────────────────┐     BizLog     │
      │                    │  Grafana(전체 추이 분석용)◀────────────────┤
 Kafka (all-log-topic)     └───────────────────────┘                │
┌─────▼────────────────────┐ 			  ┌─────────────────────────┴┐
│     Logging Service      ├─{log-index}──▶ ElasticSearch (로그 저장용) │
│- Save Log and Distribute ├──┐			  └───────────────────────▲──┘
└─────┬────────────────────┘  │	                				  │
	  │		 		          └───────────────┐             	  │
	  │	         ┌───────────────────────┐	  │             	  │
      │          │PostgreSQL(부서, 직원 정보)│	  │				      │
      │          └────────▲──────────────┘    │			          │
Kafka (sms-topic)     	  │			 Kafka (email-topic)          │
┌─────▼────────┐     ┌────┴───────┐   ┌───────▼───────┐           │
│ SMS Service  ├─────▶ Redis(캐시) ◀───┤ Email Service ◀─────┐     │
└───▲──────┬───┘     └────────────┘   └───────┬───────┘     │     │
재 전송 요청  │	                   			  │             │     │
  (SMS)    ├──────────────────────────────────┘		        │     │
	│    실패 시                                              │     │
┌───┴──────▼────┐       						            │     │
│ ErrorHandler  ├─────재 전송 요청(email)──────────────────────┘     │  
└──────┬────────┘                                                 │
	   │                                                          │
	  5회 재송신 후────────────────────────────{fail-log-index}──────┘ 

---

## 📦 주요 컴포넌트 설명

### 🔹 Mock Service
- 실제 서비스처럼 로그 이벤트를 발생시키는 테스트용 서비스
- Sidecar Agent와 함께 배포되어 로그를 전달

### 🔹 Agent
- Mock 서비스의 Sidecar로 배포되어 로그를 수집
- Kafka의 `all-log-topic`으로 로그를 전송

### 🔹 Logging Service
- Kafka에서 모든 로그를 수신하여 처리
- 모든 로그는 Elasticsearch의 `{log-index}`로 저장
- WARN, ERROR 이상 로그는 Notification Service로 라우팅

### 🔹 Notification Services (SMS, Email)
- Kafka 토픽(`sms-topic`, `email-topic`)으로부터 메시지 수신
- Redis → PostgreSQL 순으로 수신자 정보를 조회
- 전송 실패 시 Kafka의 `send-error-topic`으로 메시지를 이동

### 🔹 Error Handler
- 실패 메시지를 수신하여 최대 5회까지 재시도  
- 재시도 실패 시 Elasticsearch `{fail-log-index}`에 저장  
- 실패 사유는 리스트로 기록 (e.g. timeout, connection refused 등)

---

## 🔍 로그 수집 및 상태 감시

### EFK 구성
- 각 서비스는 Filebeat를 통해 로그를 전송  
- Elasticsearch에 저장 후 Kibana로 검색  
- BizLog / AllSystemLog 등 인덱스 분리

### 시스템 상태 감시
- Prometheus가 서비스 상태 (Actuator 기반) 수집  
- Grafana 대시보드에서 시각화  
- Alertmanager를 통해 Slack 등으로 알림 전송  

---

## ⚙️ 아키텍처 특징 요약

- ✅ Sidecar 기반 로그 수집 → 중앙 수집점으로 통합  
- ✅ Kafka pub/sub 메시징 → 비동기 알림 구조  
- ✅ Partition Key: `hash(serviceId)` → 순서 보장 및 부하 분산  
- ✅ Redis + PostgreSQL 사용자 정보 조회 최적화  
- ✅ DLQ 구조 (send-error-topic)로 실패 메시지 분리  
- ✅ Elasticsearch 로그 저장 및 Kibana 검색  
- ✅ Prometheus 기반 서비스 상태 모니터링  
- ✅ Alertmanager를 통한 Slack 알림  

---

## 🧪 실행 방법 (예정)

```bash
# 전체 시스템 실행 (docker-compose 구성 예정)
docker-compose up --build
```

> ✨ 전체 환경 구성은 docker-compose로 제공 예정이며,  
> Kafka, Redis, Elasticsearch, Prometheus, Grafana, Alertmanager 등을 포함합니다.

---

## 📚 포트폴리오 활용 포인트

- MSA 환경에서 확장 가능한 알림 시스템 구축 경험  
- Kafka 기반 비동기 메시징 구조 설계 및 구현  
- Filebeat + EFK 구조 로그 수집 파이프라인 구성  
- Prometheus + Grafana 상태 감시 시스템 연동  
- 장애 처리(DLQ, 재시도, Slack 알림) 전략 적용 경험  