# 📘 application.properties 설명서
- [⚙️ Common Setting](#common-setting)
- [📡 Actuator Setting](#actuator-setting)
- [🕰 WebClient / Scheduler Setting](#webclient-scheduler-setting)
<a name="common-setting"></a>
## ⚙️Common Setting
### 🔧 `spring.application.name`
- **타입:** `String`
- **기본값:** 없음
- **설명:** Spring 애플리케이션의 이름
- **추천 위치:** **모든 모듈**
- **활성화 시 효과:** 로그, 메트릭, Actuator 등 다양한 Spring 구성 요소에서 참조됨
- 🔸**예시**: `common`
### 🔧 `service.name`
- **타입:** `String`
- **기본값:** `__UNDEFINED__`
- **설명:** 로그 MDC에 삽입될 서비스 이름 (Spring 내부용 식별자인 spring.application.name과 다름)
- **추천 위치:** `common`, `biz` 모듈
- **활성화 시 효과:** 추적 로그(spanId) 생성시 사용됨 
- 🔸**예시**: `common`
### 🔧 `logging.interceptor.enabled`
- **타입:** `boolean`
- **기본값:** ```false``` 
- **설명:** StructuredLogInterceptor 설정 활성화 여부
- **추천 위치:** `common`, `biz` 모듈
- **활성화 시 효과:** HTTP 요청 시 MDC에 `traceId`, `spanId`, `parentSpanId` 자동 세팅됨
- 🔸**예시**: ```true```
---
<a name="actuator-setting"></a>
## 📡 Actuator Setting
### 🔧 `management.endpoints.web.exposure.include`
- **타입:** `String`
- **기본값:** 없음
- **설명:** 노출할 actuator endpoint 목록 지정
- **추천 위치:** `common`, `biz` 모듈
- **활성화 시 효과:** actuator 호출 경로를 지정 및 제한함
- 🔸**예시**: `metric,health,prometheus`
### 🔧 `management.endpoints.web.base-path`
- **타입:** `String`
- **기본값:** `/actuator`
- **설명:** actuator endpoint의 base 경로 지정
- **추천 위치:** `common`, `biz` 모듈
- **활성화 시 효과:** actuator 호출 경로를 지정
- 🔸**예시**: `/actuator`
### 🔧 `management.endpoints.health.show-details`
- **타입:** `String`
- **기본값:** 없음
- **설명:** health 상태의 세부 정보 노출 여부
- **추천 위치:** `common`, `biz` 모듈
- **활성화 시 효과**
	- 비 활성화 시 : health 상태 세부 정보 유출 방지
	- 활성화 시 : 모니터링 시스템이 정상 상태를 세부적으로 확인할 수 있게 한다
- 🔸**예시**: `always`  /  `when-authorized`  등
### 🔧 `management.metrics.export.prometheus.enabled`
- **타입:** `boolean`
- **기본값:** 없음
- **설명:** Prometheus exporter 활성화 여부
- **추천 위치:** `common`, `biz` 모듈
- **활성화 시 효과:** Prometheus가 actuator 정보를 호출할 수 있게 만듬
- 🔸**예시**: ```true```
---
<a name="webclient-scheduler-setting"></a>
## 🕰 WebClient / Scheduler Setting
### 🔧 `monitoring.scheduler.enabled`
- **타입:** `boolean`
- **기본값:** ```false```
- **설명:** Actuator 스케줄러 동작 여부
- **추천 위치:** `common`, `biz` 모듈
- **활성화 시 효과:** health/metric 정보들을 주기적으로 호출
- 🔸**예시**: ```true```
### 🔧 `monitoring.scheduler.interval.ms`
- **타입:** `long`
- **기본값:** `10000`
- **설명:** Actuator 스케줄러 주기
- **추천 위치:** `common`, `biz` 모듈
- **활성화 시 효과:** health/metric 정보들을 해당 주기마다 호출
- 🔸**예시**: `10000`
### 🔧 `monitoring.timeout.connect`
- **타입:** `long`
- **기본값:** `3`
- **설명:** Actuator Call Web Client 연결 시도 타임아웃
- **추천 위치:** `common`, `biz` 모듈
- **활성화 시 효과:** connection 시간 초과 시 warn레벨의 로그 메시지 발행
- 🔸**예시**: `3`
### 🔧 `monitoring.timeout.read`
- **타입:** `long`
- **기본값:** `3`
- **설명:** Actuator Call Web Client 응답 수신 타임아웃
- **추천 위치:** `common`, `biz` 모듈
- **활성화 시 효과:** 응답 대기 시간 초과 시 warn레벨의 로그 메시지 발행
- 🔸**예시**: `3`
---


