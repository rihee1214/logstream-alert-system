# 📝 사고 및 결정 사항 기록 템플릿 (Blank Template)

---

## 0. 결정 여부

- 적용 예정

---

## 1. 주제(Title)

SpanId 규칙 변경 및 StructuredRestController 기반 AOP 적용 계획 취소

---

## 2. 문제 인식(Problem Recognition)

- 기존에는 SpanId를 "서비스명-seq번호" 방식으로 부여하려 했으나, 동일 서비스로 다중 요청 시 식별이 어려운 문제가 예상되었다.
- 특히, 하나의 요청 흐름 안에서 동일 서비스에 여러 번 호출이 발생하면 SpanId가 중복되어 추적이 불가능해질 수 있다.
- 이를 해결하기 위해 SpanId 규칙을 "서비스명-업무명-seq번호" 형태로 변경한다.
- 또한, 기존에는 @StructuredRestController로 클래스 단위로 AOP를 걸어 MDC를 설정하려 했었다.
  - 메서드별 업무명을 명확히 지정할 필요가 생겼기에 메서드 단위로 AOP를 걸고 별도의 Annotation에서 업무명을 관리해야 하는 방향으로 변경한다.

---

## 3. 고려사항(Considerations)

- **Option 1: SpanId 규칙을 업무 단위로 세분화**
  - 장점 
    - 동일 서비스 내 다중 호출 시에도 SpanId 고유성 확보
    - 호출 트레이스 품질 및 디버깅 효율 대폭 향상
  - 단점
    - 업무명을 메서드에 일일이 지정해야 하므로 개발 초기 비용 소폭 증가

- **Option 2: Class 기반 AOP 유지**
  - 장점
    - 개발자 코드 부담 최소화
    - 지금 구현된 내용 적용으로, 변경 불필요
  - 단점
    - 업무 흐름 구분 불명확 → 추적 불가능 상황 발생 가능성 높음

---

## 4. 최종 결정(Final Decision)

**Option 1: 업무명을 SpanId에 포함하고, 메서드 단위로 MDC 세팅하는 방식으로 변경한다.**
- 선택 이유:
  - 추적성(Traceability)이 최우선이다.
  - 업무 명세 없이 단순히 서비스명만으로는 복잡한 호출 경로 복원이 어렵다.
  - 다소 개발 비용이 추가되더라도, 명확한 흐름 추적과 문제 분석이 가능한 구조를 우선한다.
- 추가 결정사항:
  - @StructuredRestController는 더 이상 사용하지 않는다.
  - 새로 만든 메서드 단위 Annotation(@StructuredMapping 등)을 이용하여 업무명(name)을 설정한다.
  - AOP 포인트컷도 클래스가 아닌 메서드 단위로 변경한다.

---

## 5. 기대효과(Expected Benefits)

- 하나의 요청 흐름 안에서도 모든 호출의 관계를 명확하게 복원 가능
- 장애 분석 및 성능 이슈 분석 시 데이터 활용성 극대화
- 장기적으로 분산 추적 시스템 연동 시에도 뛰어난 호환성 확보

---

## 6. 계속 고민할 사항(Still Open Issues)

- 초기 업무명 네이밍 기준을 어떻게 잡고, 규칙을 강조할 수 있는지

___

# ✨ 추가 확장 항목 (Optional)

## 관련 코드(Linked Code)

| 모듈(Module) | 소스 경로(Source Path) | 클래스명 (Package 포함)                                                   | 비고       |
|--------|--------------------|---------------------------------------------------------------------|----------|
| common | src/main/java      | com.rihee.alerting.common.log.aspect.StructuredMdcAspect             | 기존 방식 폐기 |
| common | src/main/java      | com.rihee.alerting.common.log.annotation.StructuredMapping          | 새롭게 작성 예정 Annotation |

## 대안 방안(Alternative Options)

- 여전히 Class 단위에 업무 흐름을 몰아넣고 세분화 없이 관리하는 방안 (단, 품질 저하 예상)

## 리스크 및 대응(Risks & Mitigation)

- name 누락 시 SpanId 생성 오류 → 기본 name을 지정하거나, 필수 입력 Validation 추가
- Annotation 미적용 시 오류 가능 → 개발자 가이드 강화 및 테스트 강화

## 추후 개정 방향(Future Improvements)

- 메서드 단위 업무명이 많아질 경우 Annotation 자동화 도구 도입 고려
- Zipkin/Jaeger 통합 대비 Span 구조 개선

---

# 📚 작성 규칙

- 가능한 간단한 문장으로 명확하게.
- 문장마다 한 가지 의미만 담을 것.
- 특히 거차한 문체보다는 가능히 실제 본인의 생각 흉름에 가까이 가게 작성할 것.

---