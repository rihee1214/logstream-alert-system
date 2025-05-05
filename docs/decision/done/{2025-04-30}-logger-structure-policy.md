---
title: logger-structure-policy
date: 2025-04-30
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
    - 관련 문서
		- **취소된 결정 :** [{2025-04-27}-structured-logger-policy.md]({2025-04-27}-structured-logger-policy.md)
		- **참고 결정1 :** [{2025-04-28}-add-parent-span-id.md]({2025-04-28}-add-parent-span-id.md)
		- **참고 결정2 :** [{2025-04-29}-spanid-policy-change-and-structured-controller-aspect-cancellation.md]({2025-04-29}-spanid-policy-change-and-structured-controller-aspect-cancellation.md)

---

## 1. 주제(Title)

AOP 기반 MDC 설정 → 메서드 단위 Structured Logging을 위한 HandlerInterceptor 전환

---

## 2. 문제 인식(Problem Recognition)

- 기존에는 `@StructuredRestController` 어노테이션과 AOP Aspect 조합을 이용하여 클래스 단위에 MDC값을 자동으로 주입하는 방식을 사용했다.
- 하지만 실제 로깅에서 중요한 것은 메서드 단위의 요청 추적이며, 특히 동일 클래스에서 복수 호출이 이뤄지는 경우, AOP만으로는 정확한 추적이 어려웠다.
- 따라서 Method 기반의 세밀한 로그 분리를 지원하는 구조가 필요했다.

---

## 3. 고려사항(Considerations)

- **기존 AOP 방식 유지**
  - 클래스 단위 처리에는 적절하나, Method 단위 spanId 분리 어려움
  - `@GetMapping` 등의 어노테이션 이름만으로 정확한 spanId 분기 어려움
  - HttpServletRequest 접근 및 Header 처리 시 불편함

- **Spring `HandlerInterceptor` 방식 전환 (최종 선택)**
  - Spring MVC의 DispatcherServlet 흐름에 자연스럽게 통합됨
  - 요청별 name 기반 spanId 지정 가능
  - 모든 HTTP 요청 진입 지점에서 일괄 MDC 세팅 가능
  - Method 단위 요청 구분에 적합

---

## 4. 최종 결정(Final Decision)

- `HandlerInterceptor`를 활용하여 Method 단위 로그 MDC 흐름을 제어한다.  
- 이전 AOP 기반 방식은 취소하며, `@StructuredGetMapping`, `@StructuredPostMapping` 등의 name 지정 기반 어노테이션을 도입한다.  
- Interceptor 내부에서 MDC 값(traceId, spanId, service 등)을 자동 세팅한다.

---

## 5. 기대효과(Expected Benefits)

- 메서드 단위 요청 흐름 추적(spanId) 명확화
- 컨트롤러 클래스 내 다수 요청에도 spanId 충돌 없이 추적 가능
- Spring DispatcherServlet의 흐름과 일관성 확보
- 유지보수 용이성 및 개발자 실수 방지

---

## 6. 계속 고민할 사항(Still Open Issues)

- 클라이언트, Gateway 등 외부 시스템과의 추적 ID 연동 구조 설계

___

# ✨ 추가 확장 항목 (Optional)

## 관련 코드(Linked Code)

| 모듈(Module) | 소스 경로(Source Path) | 클래스명 (Package 포함)                                                | 비고    |
| ---------- | ------------------ |------------------------------------------------------------------| ----- |
| common     | src/main/java      | com.rihee.alerting.common.log.aspect.StructuredMdcAspect         | 삭제    |
| common     | src/main/java      | com.rihee.alerting.common.interceptor.SpanLabelRegistry          | 신규 구현 |
| common     | src/main/java      | com.rihee.alerting.common.interceptor.SpanLabelBeanPostProcessor | 신규 구현 |
| common     | src/main/java      | com.rihee.alerting.common.interceptor.StructuredLogInterceptor   | 신규 구현 |
| common     | src/main/java      | com.rihee.alerting.common.config.WebConfig                       | 신규 구현 |

## 대안 방안(Alternative Options)

- Spring Filter 사용 (DispatcherServlet 외부 요청 흐름 제어 가능)
  - → Http Header 접근은 가능하나, Controller 정보 접근 불가 → 미채택

## 리스크 및 대응(Risks & Mitigation)

- Method 단위 spanId 자동 생성이 복잡해질 가능성 존재
  - `name` 기반 규칙을 어노테이션에 명시하도록 정책 강제

## 추후 개정 방향(Future Improvements)

---

# 📚 작성 규칙

- 문장은 간결하고 명확하게.
- 하나의 문장에는 하나의 의미만.
- 실제 생각의 흐름에 가깝게 기술할 것.

---