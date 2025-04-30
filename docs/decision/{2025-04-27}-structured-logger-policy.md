# 📝 사고 및 결정 사항 기록

---

## 0. 결정 여부

- ❌ **_결정 취소_** → [\{2025-04-30\}-mdc-handlerinterceptor-conversion.md](./{2025-04-30}-mdc-handlerinterceptor-conversion.md)
  - **취소 사유**
    - AOP 기반 클래스 단위 MDC 처리 방식 한계
    - Annotation 적용을 클래스에서 메서드로 변경함에 따라 메서드 단위 제어에 불리하여 구조 전환 필요

---

## 1. 주제(Title)

Mock 서비스의 로그 검증 및 로그 정책 설정

---

## 2. 문제 인식(Problem Recognition)

Mockup 서비스는 단순 테스트 목적이 아닌, 실제 운영 환경과 유사한 자동 로그 생성을 목적으로 한다.
이 서비스는 멀티 스레드 환경에서 돌아가며, 로그 출력 시 MDC를 통해 부가정보를 동적으로 주입한다.
그 과정에서 다음과 같은 고민이 발생했다.
- 로그의 일관성을 보장하고 검증할 별도 모듈이 필요한가?
- 개발자에게 로그 사용 규칙을 강제해야 하는가, 아니면 시스템적으로 강제해야 하는가?
- Meta 영역의 동적 주입/변경을 어떻게 안전하게 처리할 것인가?

---

## 3. 고려사항(Considerations)

- **Option 1: MemoryAppender 기반 테스트 모듈 제작**
  - 장점
    - Mock 서비스나 유닛 테스트에서 로그 일관성 자동 검증 가능
    - 예외 상황을 조기에 포착 가능
  - 단점
    - 테스트 코드 추가 및 유지 관리 부담
    - 실제 멀티 스레드 동작에 대한 완벽한 검증은 어려움
- **Option 2: AOP + Annotation 기반 StructuredLogger 자동화**
  - 장점
    - 개발자가 별도 로깅 코드나 규칙을 신경쓰지 않아도 됨
    - 휴먼 에러 최소화, 개발 교육비용 절감
    - 코드 일관성을 시스템적으로 강제 가능
  - 단점
    - AOP 설정과 MDC 관리 복잡성 증가
    - Meta 필드 같은 세부정보는 추가 고민 필요
- **Option 3: 문서화 및 개발자 교육만으로 대응**
  - 장점
    - 빠른 적용 가능
  - 단점
    - 사람에 의존 → 실수 가능성 여전
    - 품질 보장 어려움

---

## 4. 최종 결정(Final Decision)

**Option 2: AOP + Annotation 기반 StructuredLogger 자동화 방식을 선택한다.**

- **선택 이유:**
  - 개발자에게 로그 규칙을 강제하기 위해 교육하거나 수작업 검증을 하게 하면 에너지 소모가 크고, 궁극적으로 휴먼 에러를 완전히 방지할 수 없다.
  - 시스템적으로 자동화하여 일관성과 품질을 강제하는 것이 훨씬 효율적이다.
  - 초기에 AOP 설정에 대한 추가 노력이 필요하지만, 장기적으로 유지보수 비용을 줄이고, 품질을 높일 수 있다.
- **추가 결정사항:**
  - 현재 MemoryAppender는 test영역에만 남겨둔다.
    (공통 모듈로 옮길 필요성은 현재로선 낮음. Mockup 서비스 개발 중 상황을 보고 판단)

---

## 5. 기대효과(Expected Benefits)

- 로그 구조 일관성 보장
- 개발자 교육 및 관리 비용 절감
- 운영 환경에서도 structured logging 품질 향상
- Mock 서비스 품질도 실제 서비스 수준으로 끌어올림

---

## 6. 계속 고민할 사항(Still Open Issues)

- Meta 영역의 구조적 관리 방법을 어떻게 설계할지 (중간 메서드 호출 흐름에서의 안전한 제어)
- AOP + MDC 설정 충돌이나 오류 발생 시 대응 방안 마련
- 테스트 및 Mockup 서비스에서 structured logging 검증을 추가할 필요 여부

___

# ✨ 추가 확장 항목 (Optional)

## 관련 코드(Linked Code)

| 모듈(Module) | 소스 경로(Source Path) | 클래스명 (Package 포함)                                     | 비고      |
|------------|--------------------|-------------------------------------------------------|---------|
| common     | src/main/java      | com.rihee.alerting.common.log.StructuredLogger        | 실제 서비스용 |
| common     | src/main/java      | com.rihee.alerting.common.log.StructuredLoggerFactory | 실제 서비스용 |
| common     | src/test/java      | com.rihee.alerting.common.log.MemoryAppender          | 테스트 전용  |

## 대안 방안(Alternative Options)

- 공통 모듈로 MemoryAppender를 이동하여 모든 모듈에서 사용 가능하게 하는 방법

## 리스크 및 대응(Risks & Mitigation)

- AOP나 MDC 설정 오류 가능성 → 초기에 충분한 테스트, 문서화
- Meta 필드 동적 변경의 안전성 문제 → Mockup 서비스 개발 과정에서 지속적 검증 및 보완

## 추후 개정 방향(Future Improvements)

- Meta 필드에 대해 세밀한 관리 및 분리 구조 추가
- 필요시 MemoryAppender 기반 검증 기능 강화

---

# 📚 작성 규칙

- 문장은 간결하고 명확하게.
- 하나의 문장에는 하나의 의미만.
- 실제 생각의 흐름에 가깝게 기술할 것.

---

