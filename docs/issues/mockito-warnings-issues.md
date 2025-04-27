# 📋 Known Issues List

---

## 2025-04-27 | Mockito self-attach 경고 발생

- **발생 일자:** 2025-04-27
- **제목:** Mockito self-attach warning during test execution
- **상세 내용:**
    - Mockito가 inline-mock-maker를 활성화하기 위해 self-attach를 시도함
    - 이 방식은 JDK 미래 버전(23 이상)에서 기본적으로 차단될 예정
    - 현재 테스트에는 영향 없으며, JVM 옵션 `-XX:+EnableDynamicAgentLoading`으로 임시 대응 중
- **현재 상태:**
    - 무시 가능 (운영 영향 없음)
    - 단, 추후 JDK 업그레이드 시 agent 설정 필요
- **대응 방안:**
    - Mockito 공식 문서 기준에 맞춰 JVM argument 설정 추가.
- **참고 링크:**
    - [Mockito GitHub 이슈 #3037 - Self-Attaching Deprecated 논의](https://github.com/mockito/mockito/issues/3037)

---

## 🚨업그레이드 주의사항 (Upgrade Cautions)
- **JDK 업그레이드 시 주의:**
  - JDK 22~23 이후 버전에서는 -XX:+EnableDynamicAgentLoading 옵션 없이는 Mockito self-attach가 실패할 수 있음
  - Mockito mock-maker를 변경하거나, Mockito를 Java Agent로 등록하는 방법(mockito-javaagent 사용)으로 전환필요
- **SpringBoot 업그레이드 시 주의:**
  - spring-boot-starter-test가 포함하는 mockito-core, mockito-inline 버전이 업그레이드되면서 동작 방식이나 의존성 구조가 달라질 수 있음.
  - 버전 차이에 따라 mock 동작 실패, 테스트 실패 가능성이 있으므로 Spring Boot 릴리즈 노트를 반드시 확인해야 함.
- **Gradle 업그레이드 시 주의:**
  - Gradle의 JVM 옵션(jvmArgs) 적용 방식이 변경될 수 있음.
  - 테스트 태스크에 JVM 인자를 넘기는 방식(Task DSL)이 달라지거나 비호환될 수 있으므로, Gradle 릴리즈 노트를 참고하고 필요한 경우 빌드 스크립트 수정 필요.

---

# 📚 작성 규칙

- 가능한 간단한 문장으로 명확하게.
- 문장마다 한 가지 의미만 담을 것.
- 특히 거차한 문체보다는 가능히 실제 본인의 생각 흉름에 가까이 가게 작성할 것.

---
