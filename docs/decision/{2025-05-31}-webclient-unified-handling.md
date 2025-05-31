---
title: "webclient-unified-handling"
date: "2025-05-31"
status: "in-progress" # [in-progress|done|canceled]
---

# 📝 사고 및 결정 사항 기록

---

## 0. 결정 여부

- 🔁 적용 예정
	- 작업자 : 리희
	- 작업 완료 예정일 : ASAP
	- 작성자 : 리희
	- 참석자 : 없음
    - 관련 문서 : [관련 문서명](경로)


---

## 1. 주제(Title)

WebClient 요청 및 응답 처리 전략 통합

---

## 2. 문제 인식(Problem Recognition)

현재 mockup 서비스에서는 외부 시스템 호출(WebClient 사용)에 대한 처리 로직이 중복되어 있으며, 다음과 같은 문제점이 존재한다.

- 요청 및 응답 로그 처리 방식이 서비스마다 상이함
- MDC 세팅 누락, StopWatch 누락 등 정책 위반 가능성 존재
- WebClient 예외 처리와 fallback 응답 처리 방식도 통일되어 있지 않음
- 코드 중복으로 인해 추후 정책 변경 시 유지보수 비용이 급격히 증가함

---

## 3. 고려사항(Considerations)

- **Option 1: 모든 WebClient 요청/응답 처리를 단일 방식으로 강제하고, 필요 시 오버라이딩**
	- 장점:
	    - 정책 통일, 오류 감소
	    - 로깅/응답/예외 처리 일괄 관리 가능
	    - 정책 변경 시 단일 코드 수정만으로 전체 반영 가능
	    - 각 모듈 개발자는 해당 클라이언트 코드만 이용하면 됨
	- 단점:
	    - 구조화된 WebClient 래퍼나 Handler 작성 필요
	    - 일반적인 WebClient 사용 방식과 다소 다름
	    - 개발자에게 제약이 생김

- **Option 2: 각 모듈 개발자가 WebClient를 자율적으로 작성**
	- 장점:
	    - 자유로운 코드 작성 가능
	    - 단순한 호출에는 구현 부담이 적음
	- 단점:
	    - 요청/응답 처리 누락 가능성 매우 큼
	    - 일관된 정책 적용 어려움
	    - 코드 중복 심화 및 유지보수 어려움

---

## 4. 최종 결정(Final Decision)

**Option 1 채택**
- 모든 WebClient 요청 및 응답 처리 방식은 공통 코드에서 일괄 관리
- 로그 기록, MDC 세팅, StopWatch, fallback 등은 기본 구현에 포함
- 각 모듈 개발자는 이 Wrapper 또는 Client 클래스만 사용하도록 유도
- 필요 시 오버라이딩 또는 Hook으로 확장 가능

---

## 5. 기대효과(Expected Benefits)

- 외부 시스템 호출 정책의 완전한 통일
- 코드 중복 제거 및 유지보수 용이
- 개발자 실수 감소 및 정책 위반 방지
- 장애 발생 시 추적 및 대응 능력 향상
- 테스트 및 로깅 정책 검증의 일관성 확보

---

## 6. 계속 고민할 사항(Still Open Issues)

- 응답 로그의 로그타입(LogType)을 `SYS`로 할지 `BIZ`로 할지 정책 정리 필요
- Retry, CircuitBreaker, Timeout 등의 고급 기능 연동 여부
- Hook/Decorator 방식으로 확장 포인트 제공 여부

---

# ✨ 추가 확장 항목 (Optional)

## 관련 코드(Linked Code)

_(관련 소스파일, 클래스명을 정리한다)_

| 모듈(Module) | 소스 경로(Source Path) | 클래스명 (Package 포함) | 비고 |
|--------------|------------------------|--------------------------|------|
|              |                        |                          |      |

## 대안 방안(Alternative Options)

- WebClientCustomizer나 Filter를 이용한 부분 자동화
	- 단순 처리에는 유용하나 복잡한 MDC, exception, 응답 로그 등까지는 처리 한계 있음

## 리스크 및 대응(Risks & Mitigation)

- ❗ 개발자에게 학습이 필요함 → 내부 개발 가이드 제공
- ❗ 자유도 부족으로 불만 발생 가능성 → 오버라이딩 허용 및 Hook 구조 도입 고려

## 추후 개선 방향(Future Improvements)

- 공통 응답 구조(Wrapper Response DTO) 도입
- 응답 body 로그 여부에 대한 정책 추가
- 외부 시스템에 대한 Trace ID 헤더 전파 정책 명확화

---

# 📚 작성 규칙

- 문장은 간결하고 명확하게.
- 하나의 문장에는 하나의 의미만.
- 실제 생각의 흐름에 가깝게 기술할 것.

---
