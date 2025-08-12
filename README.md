# 📊 Distributed Centralized Logging & Monitoring Platform

> MSA 환경에서 다수의 서비스가 생성하는 로그와 메트릭을 **중앙에서 수집·저장·분석·알림**까지 처리하는 확장 가능한 플랫폼  
> Biz 서비스의 로그·메트릭을 안전하고 일관된 방식으로 수집하고, 분석과 모니터링을 통해 운영 가시성을 극대화합니다.

---

## 🎯 프로그램 목적

이 플랫폼의 목표는 **서비스 운영의 투명성과 안정성을 보장**하는 것입니다.  
단일 서비스 수준의 로깅을 넘어, **조직 전체 MSA 환경**에서 발생하는 모든 로그·메트릭을 중앙집중화하여 다음을 실현합니다:

1. **표준화된 로그 수집**
   - Biz Component(라이브러리/정책)를 통해 각 서비스가 동일 포맷으로 로그를 생성
   - Interceptor, MDC 전파, 필터링 규칙 등 사전 처리 내장

2. **유연한 데이터 수집 경로**
   - Fluent Bit + Lua 스크립트를 통한 컨테이너 로그 수집 및 전처리
   - Collector/Validator/Transformer/Persistence 구조로 구성 요소 교체 가능

3. **안정적인 중앙 저장소**
   - 메시지 브로커(Kafka)와 영속 저장소(PostgreSQL 기본)
   - 메시지 키 일원화(LogMessageKeyGenerator)로 중복 방지

4. **운영 가시성 확보**
   - 메트릭 수집(Prometheus)과 대시보드(Monitoring Service)로 실시간 상태 확인
   - 로그 검색·조회 인터페이스 제공

5. **능동적인 알림 체계**
   - 규칙 기반 Alerting Service를 통해 이상 징후를 Slack/Email 등으로 즉시 전달

---

## 🛠 기술 스택

| 영역         | 사용 기술                                    | 설명                              |
|------------|------------------------------------------|---------------------------------|
| Language   | Java 21                                  | 최신 LTS 버전의 JVM                  |
| Framework  | Spring Boot 3.4.x                        | REST API, Kafka Consumer, 설정 관리 |
| Messaging  | Apache Kafka                             | 로그 전달용 메시지 브로커                  |
| Database   | PostgreSQL                               | 로그 영속 저장소                       |
| Build Tool | Gradle (Groovy DSL)                      | 멀티 모듈 빌드 관리                     |
| Monitoring | Spring Boot Actuator, Prometheus (기본 설정) | 헬스 체크 및 기본 지표 수집                |
| Deployment | Docker Compose                           | 로컬 개발 및 테스트 환경 구성               |

---

## 🛠 사용 도구

| 영역                   | 사용 도구         | 도구 설명                                            |
|----------------------|---------------|--------------------------------------------------|
| JDK                  | OpenJDK 21    | 컨테이너 환경을 고려해 최신 LTS 버전인 Java 21 사용               |
| IDE                  | IntelliJ IDEA | 복잡한 멀티 모듈 구조를 효율적으로 관리하기 위한 통합 개발 환경             |
| Documents Management | Obsidian      | 구조적 문서화를 중시하여, 작성·탐색·관리가 용이한 Obsidian으로 문서 관리 수행 |

---


## 🏗 현재 아키텍처 구성도

![Logging Service Architecture](./docs/01.%20architecture/MsaLogHandleProjects.drawio.svg)


---

## 📦 모듈 구성 개요

본 플랫폼은 운영 환경에서 **로그 수집 → 모니터링 → 알림** 흐름을 지원하며,  
각 단계별로 모듈을 독립적으로 설계하여 확장성과 유지보수성을 높였습니다.

| 모듈 | 역할 | 상태 |
|------|------|------|
| **로그 수집기 (Logging Collector)** | Biz 서비스 로그를 표준 포맷으로 수집·저장 | MVP 구현 완료 |
| **모니터링 (Monitoring)** | 서비스 및 로그 상태를 실시간으로 관찰·시각화 | 개발 예정 |
| **알림 (Alerting)** | 규칙 기반으로 이상 징후를 감지·전파 | 개발 예정 |

---

## 1️⃣ 로그 수집기 (Logging Collector)

### 구성 요소
- **Biz Component**  
  Biz 서비스에서 로그를 생성·전송하기 위한 라이브러리/정책 모듈(framework).  
  Interceptor, MDC 전파, 필드 필터링 규칙 등을 포함.
- **Fluent Bit**  
  컨테이너 로그 수집기. 필요 시 Lua 스크립트를 통해 전처리 수행.
- **Kafka**  
  로그 전송을 위한 메시지 브로커.
- **Logging Service**  
  Collector → Validator → Transformer → Persistence 구조의 파이프라인을 통해 로그를 가공·저장.
  `LogMessageKeyGenerator`를 이용해 메시지 중복 방지.
- **PostgreSQL**  
  영속 저장소.

### 동작 흐름
1. Biz Component가 지정된 포맷의 로그를 출력
2. Fluent Bit이 로그를 수집하고 Kafka로 전달
3. Logging Service가 Kafka 메시지를 받아 파이프라인 처리
4. 가공된 로그를 PostgreSQL에 저장

---

## 2️⃣ 모니터링 (Monitoring) *(개발 예정)*

> 모듈 구성 및 상세 내용은 개발 완료 후 업데이트 예정입니다.

---

## 3️⃣ 알림 (Alerting) *(개발 예정)*

> 모듈 구성 및 상세 내용은 개발 완료 후 업데이트 예정입니다.

---