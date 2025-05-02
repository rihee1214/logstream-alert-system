# 작업 해야할 목록

- [ ] spanId 변경 관련 AOP, Interceptor 구조 재검토
	- [x] 인터셉터 관련 코드 작성
	- [ ] 인터셉터 관련한 테스트 모듈 고려
	- [ ] common영역의 AOP관련된 코드 세개 정리필요
- [ ] git, github branch 전략 고민하기
- [ ] checkstyle 제대로 동작 안하는 현상 고쳐야함
- [ ] SpanLabelBeanPostProcessor의 postProcessAfterInitialization메서드 리팩토링
# 이미 작업한 목록
- [x] docs 템플릿 Obsidian화
- [x] 사용하고 있는 tool에 대한 목록 README 정리
  - [x] JDK (openjdk 21)
  - [x] IDE (intellij)
  - [x] Obsidian
- [x] 기존 로그 구조 리팩토링 필요성 분석 및 로거 인터페이스 분리 (새로운 로거 추가 및 정책 변경시 변경의 용이성을 획득하기 위함)
- [x] StructuredLoggerImpl class는 외부에서는 보이지 않고 interface를 통해 사용하도록 해야함.
	- [x] 그렇게 하는 방법이 없어서 그냥 interface에 주석만 달았음