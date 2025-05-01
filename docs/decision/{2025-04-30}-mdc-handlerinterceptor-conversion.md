---
title: mdc-handlerinterceptor-conversion
date: 2025-04-30
status: in-progress
---

# 📝 사고 및 결정 사항 기록

---

## 0. 결정 여부

- 🔁 적용 예정
	- 작업자 : 이리희
    - 작업 완료 예정일 : ASAP
    - 작성자 : 이리희
    - 참석자 : 이리희
    - 관련 문서 : n/a

---

## 1. 주제(Title)

StructuredLogger의 logger 생성 방식 및 KafkaAppender 적용 정책 결정

---

## 2. 문제 인식(Problem Recognition)

- 기존에는 클래스별로 logger를 생성 (LoggerFactory.getLogger(getClass())) 하여 로그에 자동으로 클래스명이 포함되도록 구성했다.
  - KafkaAppender는 logger 수가 많아질수록 내부 큐, 전송 쓰레드 등에서 병목 가능성이 높다.
  - MSA 환경에서는 로그 sink 병목이 서비스 전체 성능에 영향을 줄 수 있다.
- 이에 따라 logger 인스턴스를 biz, sys 두 개로 제한함으로써, KafkaAppender 중심의 로깅 구조를 단순하고 안정적으로 유지하고자 한다.
  - 이 방식에서는 로그에 클래스명이 포함되지 않기 때문에, 로그 메시지의 출처 추적이 어려워지는 단점이 존재한다.

---

## 3. 고려사항(Considerations)

- **Option 1: 클래스별 logger 생성 유지**
  - 장점: SLF4J 표준 구조, 자동으로 클래스명 포함
  - 단점: logger 인스턴스 수 증가 → KafkaAppender 병목 가능성
- **Option 2: logger를 biz/sys 두 개로 고정 (최종 선택)**
  - 장점: KafkaAppender 효율 유지, StructuredLoggerFactory 추상화 용이
  - 단점: 클래스명은 MDC를 통해 별도 관리 필요 → StructuredLogger 구현 복잡성 증가

---

## 4. 최종 결정(Final Decision)

- StructuredLoggerFactory.getBizLogger(), StructuredLoggerFactory.getSysLogger() 방식으로 logger 인스턴스를 제한한다.
- 클래스명은 로깅 파라미터로 전달하여 MDC에 자동 주입한다.
- 인터페이스는 StructuredLogger.info(String message) 형태로 단순화하고, 내부에서 caller class를 추적하여 class명을 MDC에 넣는 구조로 설계한다.
- KafkaAppender 설정은 공통 appender로 구성하며, buffer 오버 시 block timeout 설정 및 fallback 대응도 함께 구성한다.
- 이 결정은 단순한 기술적 효율성 확보가 아니라, **비즈니스 로직 중심의 트랜잭션 흐름을 정확히 추적하고 분석 가능한 로깅 구조를 설계하는 데 중점을 둔 판단이다**.
  - 많은 로그가 발생하는 시스템은 오히려 **트래픽 과부하 상태로 간주**되어야 하며,
  - 정상적이고 예측 가능한 요청 흐름 속에서의 로그가 추적성과 분석력을 높인다.
  - 따라서 **logger 수는 줄이되, 컨테이너 레벨의 수평 확장과 분산 로그 추적 구조를 병행**하는 방향으로 구성한다.

---

## 5. 기대효과(Expected Benefits)

- KafkaAppender 병목 가능성 최소화
- StructuredLogger 구조의 일관성 확보
- 향후 logtype 추가 시 변경 최소화
- 로그 분산 추적을 위한 class/source 정보는 여전히 확보 가능
- **비즈니스 로직 중심의 트랜잭션 추적을 위한 로깅 구조로 최적화**
  - logger 수가 많아지는 구조가 아니라, **비즈니스 흐름에 맞게 통제된 요청 흐름이 로그로 반영됨**
  - 로그가 지나치게 많이 생성되는 구조는 오히려 시스템 부하를 의미하며, 그 시점에는 **MSA 레벨에서 복제/확장으로 대응함**

---

## 6. 계속 고민할 사항(Still Open Issues)

- KafkaAppender 전송 실패 시 fallback 구조 구성 여부
- Filebeat 기반 구조로의 전환 가능성 및 대비

___

# ✨ 추가 확장 항목 (Optional)

## 관련 코드(Linked Code)

| 모듈     | 소스 경로         | 클래스명 (Package 포함)                                     | 비고                |
|--------|---------------|-------------------------------------------------------|-------------------|
| common | src/main/java | com.rihee.alerting.common.log.StructuredLogger        | 인터페이스             |
| common | src/main/java | com.rihee.alerting.common.log.StructuredLoggerBizImpl | Logger biz 분기     |
| common | src/main/java | com.rihee.alerting.common.log.StructuredLoggerSysImpl | Logger sys 분기     |
| common | src/main/java | com.rihee.alerting.common.log.StructuredLoggerFactory | 클래스명 MDC 주입 로직 포함 |

## 대안 방안(Alternative Options)

- Filebeat 기반 로그 수집 구조로 전환
  - 클래스별 logger 영향 최소화로 수정 필요없게 함
  - KafkaAppender 제거

## 리스크 및 대응(Risks & Mitigation)

- KafkaAppender 버퍼 오버플로 시 서비스 블락 가능성 → max.block.ms, delivery.timeout.ms 등 제한값 설정

## 추후 개정 방향(Future Improvements)

- Filebeat 기반 아키텍처로 전환될 경우 전체 구성을 유지하면서 전환 가능하도록 구성 고려

---

# 📚 작성 규칙

- 문장은 간결하고 명확하게.
- 하나의 문장에는 하나의 의미만.
- 실제 생각의 흐름에 가깝게 기술할 것.

---