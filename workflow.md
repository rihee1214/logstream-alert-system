# 🛠 Distributed Alerting System - 개발 워크플로우 (순차 진행 계획)

> 약 6개월의 사이드 프로젝트 목표  
> 하루 1시간 또는 여유 시간에 맞춰 천천히 진행  

---

## 🚀 프로젝트 초기 세팅 & 기본 흐름 만들기

### SpringBoot 멀티모듈 구성 및 Kafka/Redis 환경 세팅

1. Gradle 멀티모듈 프로젝트 구성  
2. Kafka 로컬 실행 + 테스트  
3. Redis 로컬 실행  
4. 공통 설정 모듈 분리  
5. GitHub Repo 생성 및 초기 커밋  

---

### Kafka 통신 흐름 구성 (Mock Service ↔ Agent ↔ Logging)

6. Mock 서비스 생성  
7. Agent (Sidecar 구조) 구현 – REST 수신  
8. Logging 서비스 구현 – Kafka Producer 연동  
9. Kafka Listener 로직 테스트  
10. end-to-end 로그 흐름 점검  

---

## 🔔 알림 서비스 흐름 구현

### 로그 필터링 → Kafka publish (WARN/ERROR)

11. 로그 메시지 레벨 분기 처리  
12. ManagementService 구성  
13. sms-topic / email-topic 발행 분리  
14. 메시지 키 패턴 적용 (serviceId-timestamp 등)  
15. Kafka 메시지 흐름 전체 테스트  

---

### 알림 서비스 & 실패 처리 흐름 구현

16. SMS 알림 서비스 구현 (Kafka Consumer)  
17. Email 알림 서비스 구현 (Kafka Consumer)  
18. 전송 실패 시 send-error-topic으로 이동  
19. ErrorConsumer 구현 → 실패 메시지 DB 저장  
20. 재전송 구조 설계 및 구성  

---

## 🗄 저장소 구성 (RDB + Redis)

### 로그 저장소 – RDB 기반

21. 로그 저장용 테이블 설계  
22. JPA 기반 로그 저장 로직 구현  
23. 레벨별 저장 여부 구분 처리  
24. DB 조회 및 테스트  

---

### 사용자 정보 캐시 – Redis 기반

25. 직원 정보 테이블 설계  
26. Redis 키 구조 설계 (Key, TTL 등)  
27. Redis에서 조회하고, miss 시 DB fallback 처리  
28. 캐시 갱신 흐름 구현  
29. 캐시 hit/miss 시나리오 테스트  

---

## ♻️ 실패 처리 및 재전송 로직 정리

### send-error-topic → DB 기반 재처리

30. 실패 메시지 재전송 큐 구성  
31. 전송 여부 관리용 `send_yn` 컬럼 설계  
32. 관리자 수동 재전송 흐름 구현  
33. 재시도 성공 시 상태 변경 처리  

---

### 전체 흐름 리팩토링 & 통합 테스트

34. 전체 메시지 흐름 정리 (Mock → 알림까지)  
35. Mock 메시지 자동 발행 설정  
36. end-to-end 통합 흐름 검토  
37. 버그 및 누락 포인트 수정  

---

## 🐳 배포 및 자동화 구성

### Docker 기반 로컬 배포 환경

38. 각 서비스 Dockerfile 작성  
39. docker-compose 구성  
40. Kafka, Redis 포함 전체 서비스 실행  

---

### CI 자동화 및 테스트 구성

41. GitHub Actions 설정 (Test → Build)  
42. 기본 테스트 코드 구성  
43. 테스트 커버리지 측정  
44. 로컬에서 전체 테스트 실행  

---

## 📑 로그 수집 및 EFK 구성

### 시스템 로그 → Filebeat → Elasticsearch

45. 각 서비스 로그 파일 위치 확인  
46. Filebeat 설정 (로그 수집)  
47. Filebeat → Elasticsearch 파이프라인 구성  
48. `log-index`, `fail-log-index` 매핑 구성  
49. Kibana 시각화 테스트  

---

## 📈 시스템 상태 감시 구성 (선택 사항)

50. Spring Boot actuator 활성화  
51. Prometheus + Node Exporter + Kafka Exporter 구성  
52. Prometheus → Grafana 연동 및 대시보드 생성  
53. Alertmanager 알림 시나리오 구성  
54. 서비스 다운 / 로그 spike 알림 시나리오 구현  

---

## 🧪 운영 시나리오 통합 테스트

55. Mock 서비스 고빈도 로그 발행 시나리오  
56. SMS/Email 실패 유도 → ErrorHandler 흐름 검증  
57. Elasticsearch 로그 (log-index, fail-log-index) 확인  
58. Kafka 오프셋 및 메시지 순서 확인  
59. 시스템 장애 복구 시나리오 테스트  

---

## 📚 문서화 및 포트폴리오 정리

### 시스템 문서화

60. 각 모듈 역할 및 README 작성  
61. 기술 스택 및 구조 설계 이유 문서화  
62. 장애/에러 처리 전략 요약  

### 포트폴리오 마무리

63. 설계 흐름 다이어그램 정리 (draw.io 등 활용)  
64. GitHub 구조 정리 (README, 링크 등)  
65. 이직용 포트폴리오 요약 문서 작성  
