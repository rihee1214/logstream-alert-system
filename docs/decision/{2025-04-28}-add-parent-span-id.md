# 📝 사고 및 결정 사항 기록

---

## 0. 결정 여부

- ✅ 적용 완료

---

## 1. 주제(Title)

ParentSpanId 필드 추가 결정

---

## 2. 문제 인식(Problem Recognition)

서비스 간 통신이 이루어질 때, 단순히 spanId 만으로는 요청 흐름을 완벽하게 추적할 수 없다.
특히 한 서비스가 여러 다른 서비스를 호출하는 구조에서는 호출 경로를 알기 어렵거나, 사실상 불가능해진다. 
이를 해결하기 위해 부모 spanId를 별도로 저장하여, 호출 관계를 명확히 기록하고자 한다.

---

## 3. 고려사항(Considerations)

- **Option 1: ParentSpanId 추가**
  - 장점 
    - 복잡한 호출 구조에서도 요청 흐름 추적 가능
    - 시스템 디버깅과 장애 분석 시 유리
  - 단점
    - 로깅 필드 추가로 인한 약간의 오버헤드
    - MDC 및 로그 구조 관리 복잡성 약간 증가

- **Option 2: 기존 SpanId만 유지**
  - 장점
    - 구현 간단, 추가 필드 관리 불필요
  - 단점
    - 복잡한 호출 트레이스를 복원하기 매우 어렵거나 불가능

---

## 4. 최종 결정(Final Decision)

**Option 1: ParentSpanId를 추가하기로 결정하였다.**
- 선택 이유:
  - 시스템 규모가 커지면서 호출 트리 복원이 필수적이 되었기 때문이다.
  - 초기 구현은 약간 번거롭지만, 추후 장애 분석/모니터링 시 압도적인 이점을 가진다.

---

## 5. 기대효과(Expected Benefits)

- 호출 간 관계 파악이 쉬워져 장애 원인 추적 및 분석 시간 단축
- 전체 서비스 간 통신 흐름 가시성 향상
- 추후 분산 추적 시스템(예: Zipkin, Jaeger 등) 연계 준비

---

## 6. 계속 고민할 사항(Still Open Issues)

- ParentSpanId가 없는 최초 요청(외부 트래픽)의 경우 어떤 값을 넣을지
- SpanId, ParentSpanId 생성 및 전파를 얼마나 강제할지
- 외부 시스템 연동 시(예: 3rd Party API 호출) ParentSpanId 관리 방법

___

# ✨ 추가 확장 항목 (Optional)

## 관련 코드(Linked Code)

| 모듈(Module) | 소스 경로(Source Path) | 클래스명 (Package 포함)                                        | 비고                           |
|------------|--------------------|----------------------------------------------------------|------------------------------|
| common     | src/main/java      | com.rihee.alerting.common.log.aspect.StructuredMdcAspect | MDC 초기화 및 ParentSpanId 적용 예정 |

## 대안 방안(Alternative Options)

- SpanId만 관리하고, 호출 트리 복원은 별도 시스템(예: APM)을 이용하는 방안 (단, 시스템 독립성 약화)

## 리스크 및 대응(Risks & Mitigation)

- Span/ParentSpan 관리 실패 시 흐름 추적 불가 → 모든 API 진입점에 강제 정책 적용 필요
- 불필요한 필드 추가 오버헤드 → JSON 최적화 및 압축 설정 고려

## 추후 개정 방향(Future Improvements)

- SpanId/ParentSpanId를 기반으로 한 간단한 호출 트레이스 UI 제공 고려
- SpanDepth 등의 추가 정보 도입 검토 가능

---

# 📚 작성 규칙

- 문장은 간결하고 명확하게.
- 하나의 문장에는 하나의 의미만.
- 실제 생각의 흐름에 가깝게 기술할 것.

---