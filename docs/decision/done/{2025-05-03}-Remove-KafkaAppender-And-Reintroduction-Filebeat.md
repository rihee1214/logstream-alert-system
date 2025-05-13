---
title: Remove-KafkaAppender-And-Reintroduction-Filebeat
date: 2025-05-03
status: done
---

# 📝 사고 및 결정 사항 기록

---

## 0. 결정 여부

- ✅ 적용 완료
    - 작업자 : 이리희
    - 완료 일자 : 2025-05-05
    - 작성자 : 이리희
    - 참석자 : 이리희
    - 관련 문서 : n/a

---

## 1. 주제(Title)

KafkaAppender 사용 금지 및 Filebeat 재도입 결정

---

## 2. 문제 인시(Problem Recognition)

- Logback의 KafkaAppender는 유지보수가 수년째 중단된 상태이다.
- MDC 기반 라우팅 구조를 적용하기 어려워 복잡한 로직을 수반하며, 구조 확장성이 떨어진다.
- Appender에 filter 설정이 불가능하고, 고도화된 필터링 전략이 제한된다.
- 로그 라우팅, 관리, 성능 측면에서 제약이 많아 콘솔 기반 로깅에 Filebeat 연동 방식으로 회귀를 고려하게 됨.

---

## 3. 고려사항(Considerations)

- **Option 1: **KafkaAppender 유지**
	- 장점: 외부 툴 없이 Kafka 직행 가능
	- 단점: 라우팅 제약, 유지보수 중단, 테스트 환경 적용 어려움, 성능 이슈 발생 가능성

- **Option 2: **Console/FileAppender + Filebeat 사용**
	- 장점: 구조 단순화, 라우팅/필터링 자유로움, 유지보수 활발한 Filebeat 사용 가능, 유연한 로그 전송 시점 제어
	- 단점: 로그 수집 지연 가능성, 로그 유실 가능성에 대한 별도 고려 필요

---

## 4. 최종 결정(Final Decision)

_KafkaAppender는 사용하지 않고 Console 또는 FileAppender를 통해 로그를 출력한 뒤, Filebeat를 이용해 Kafka로 전달한다._

---

## 5. 기대효과(Expected Benefits)

- 로그 라우팅 구조 단순화
- 유지보수 가능한 오픈소스 도구 중심으로 구성 가능
- 로그 유실 시 수동 재처리 가능 (파일 기반)
- 테스트 환경과 운영 환경 간 구성 일관성 확보

---

## 6. 계속 고민할 사항(Still Open Issues)

- 로그 유실 방지 및 Filebeat 버퍼링 설정 최적화
- 컨테이너 종료 전 로그 유실 방지를 위한 graceful shutdown 처리

---

# ✨ 추가 확장 항목 (Optional)

## 대안 방안(Alternative Options)

- **Logstash 직접 연동**: 로그 수집기 역할을 대체할 수 있으나 리소스 과다 소비 우려로 배제
- **Logback 확장 구현 유지**: CompositeRoutingAppender 직접 구현 시 유지보수 어려움으로 배제

---

# 📚 작성 규칙

- 문장은 간결하고 명확하게.
- 하나의 문장에는 하나의 의미만.
- 실제 생각의 흐름에 가깝게 기술할 것.

---
