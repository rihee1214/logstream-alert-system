# 작업 해야할 목록

#task
- [ ] git, github branch 전략 고민하기
- [ ] checkstyle 제대로 동작 안하는 현상 고쳐야함
- [ ] Mockup서비스 구축
	- [ ] controller, service, dao 단계로 동작하도록 처리
	- [ ] db연결은 굳이 필요없고, db에서 select한 것처럼 동작하게 만들기.
	- [ ] 한 controller 여러개의 controller를 호출하게 만들고 추적이 되는지 확인 필요
	- [ ] scheduler가 돌면서 계속 특정 controller를 call하여 로그가 계속 쌓이도록 만들기
- [ ] 문서 작성 필요 요소
	- [ ] Actuator Filter관련된 설명을 추가하고, Https만 사용할 것을 강조하기
- [ ] docker, docker-compose.yml, kubernetes.yml 파일에 주석과 환경 변수 넣을 요소
	- [ ] Common 영역
		- [ ] HOST, CONTAINER 명을 환경 변수로 넣어 주어야 함
		- [ ] Actuator를 Call하는 prometheus 인증 헤더 토큰(환경변수 key값: monitoring.token)
	       (HTTP header = X-Monitoring-Token)을 환경 변수로 넣어야 한다는 내용 문서화 필요.
- [ ] Actuator call(metric call) 하는 scheduler에 대한 로직 수정 필요.
	- [ ] configMap -> 파일 마운트 방식 (properties)
		- [ ] 가장 기본적인 항목들 (common에 구현된 사항)은 문서화 해야함
	- [ ] properties는 여러 이유로 polling 방식을 사용하도록 결정
		- [ ] 문서화 필요(WatchService 포기)
			- [ ] 가상 컨테이너 환경에서 적절하게 동작하지 않을 가능성 높음
			- [ ] 파일이 크지 않기 때문에 그리 부담되는 작업이 아님
	- [ ] 모든 생성된 코드에 javadoc 작성하기
	- [ ] 문서화 필요
		- [ ] 새로운 handler만들고, 적용 방식은 알아서 하도록 지시하기 (만드는 방법, 주의사항)
		- [ ] 처리 로직에 대한 설명 추가
	- [ ] 구현 마무리하기
	- [ ] 모든 작업은 비동기로 처리가 되어야 한다.
	- [ ] build.gralde 에 제대로 된 설정 만들어 넣기
	- [ ] 테스트 로직 구현 완성도 필요
- [ ] 필요한 properties가 많아지고 있음. java option으로 넣어주는 properties를 모든 컨텍스트가 초기화 되기 전에 검증할 수 있도록 해야함.
	- [ ] 다만 문제는 모든 모듈에서 각자가 가지고 있는 properties만 검증하게 하려면 main에서 호출하게 만드는 것은 문제가 있음 (의존 대상자의 validate는 되지 않기 때문)
---
# 이미 작업한 목록
#done
- [x] docs 템플릿 Obsidian화
- [x] 사용하고 있는 tool에 대한 목록 README 정리
	- [x] JDK (openjdk 21)
	- [x] IDE (intellij)
	- [x] Obsidian
- [x] 기존 로그 구조 리팩토링 필요성 분석 및 로거 인터페이스 분리 (새로운 로거 추가 및 정책 변경시 변경의 용이성을 획득하기 위함)
- [x] StructuredLoggerImpl class는 외부에서는 보이지 않고 interface를 통해 사용하도록 해야함.
	- [x] 그렇게 하는 방법이 없어서 그냥 interface에 주석만 달았음
- [x] mockup을 제외한 나머지 모듈들이 오류가 발생했을때 로그를 어떻게 처리해야할지 고민하기
      (그저 alertmanager에게만 의존하기로 결정)
- [x] KafkaAppender를 제거하고 다시 FileBeat를 사용하도록 README.md파일 수정
	- [x] 기술 스택에 다시 등록하기
	- [x] 아키텍처 구성도 수정하기
	- [x] 내용 다시 읽어보고 다시 원래대로 구성하기
- [x] bizlog-contract, syslog-contract 수정하고 actlog-contract작성
- [x] SpanLabelBeanPostProcessor의 postProcessAfterInitialization메서드 리팩토링
- [x] spanId 변경 관련 AOP, Interceptor 구조 재검토
	- [x] 인터셉터 관련 코드 작성
	- [x] 인터셉터 관련한 테스트 모듈 작성
	- [x] common영역의 AOP관련된 코드 세개 정리필요
- [x] actuator 코드 작성하기 (모든 시스템 전용)
      (property 세팅만 잘 되어있으면  됨)
- [x] Actuator는 localhost에서만 call할 수 있다던가 하는 설정 추가할 수 있게 만들기
- [x] 모든 환경 변수, property 세팅의 default값은 `__UNDEFINED__`임을 기록하고, 그것을 따로 뺄 궁리하기
- [x] google java 스타일의 checkstyle 적용으로 인해 .editorconfig 설정 바꿔서 에디터 설정 바꿔주기
	- [x] 기본 구성 완료
	- [x] 더 확장해서 자세한 설정 추가 작성하기
- [x] Actuator call 로그 남기게 만들기
- [x] 비지니스 서비스 호출에 대한 B3Header에 관련된 내용 추가하기.
- [x] bizlog-contract문서에 meta영역에  B3Header관련 내용 추가하기
	- [x] Meta영역에 sampled와 flags 요소가 들어가게됨
- [x] application.properties 문서에 주석 다듬기
	- [x] Common 영역
		- [x] service명(service.name) 넣어주어야 함
		- [x] application.properties에 monitoring.scheduler.interval.ms를 넣어주어야. actuator call 간격 설정 가능함을 알려야함
		- [x] Actuator 설정 관련 문서 남기기 (application.properties에 들어갈 항목 넣어주어야 함)
- [x] scheduleing관련된 코드 및 기존 테스트 코드 다듬기