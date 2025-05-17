# 📦 Biz 모듈 (Mockup Service) 개발 가이드

## 📌 개요
Biz 모듈은 독립적인 비즈니스 기능을 제공하며, 로그를 Kafka 기반 파이프라인으로 전송합니다.

---
## 🔧 개발 시 유의사항
- 로그는 반드시 `StructuredLoggerFactory`를 통해 생성된 로거를 사용해야 합니다.
- MDC 설정을 보존하기 위해 사용자 정의 Interceptor는 `order > 0`으로 설정합니다.
- **❗모든 HTTP 요청에는 반드시 [B3Header](https://github.com/openzipkin/b3-propagation#multiple-headers)를 포함해야 합니다.**
	- 이는 로그 추적(Trace)를 위한 필수 요건이며, 미설정 시 로그 연계 및 추적 기능이 **정상 동작하지 않습니다.**
---
## 🧩 공통 모듈 확장 지점 (Optional Extension Points)
공통 모듈은 기본적인 structured logging 기능을 제공합니다.  
다만, 특정 비즈니스 상황에서는 여러 요소들에 대한 정책을 개별적으로 정의할 필요가 있습니다.

### 1. 로그 추적을 위한 `traceId`, `spanId`, `parentSpanId` 생성 규칙 커스터마이징

- 기본 구현 클래스:  
    `com.rihee.alerting.common.interceptor.DefaultStructuredLogInterceptor`
    
- 커스터마이징 방법:  
    `com.rihee.alerting.common.interceptor.AbstractStructuredLogInterceptor`를 상속  
	    - `generateTraceId(String)`
	    - `generateSpanId(String, String)`
	    - `generateParentSpanId(String)`를 오버라이드
    
- Bean 설정 예시:  
    `com.rihee.alerting.common.interceptor.StructuredLogInterceptorFactory`를 구현한 Factory Bean을 Biz 모듈에 등록하면 해당 로직이 자동으로 사용됨

```java
@Bean  
public StructuredLogInterceptorFactory customFactory() {  
   return (registry, serviceName) 
		   -> new MyCustomInterceptor(registry, serviceName);  
}  
```
---
## ⚠️ 설계 주의사항
- `StructuredLoggerFactory`로 생성한 로거는 **오직 비즈니스 서비스에서만 사용**해야 합니다.
	- 공통 모듈(common), logging-service 등에서는 SLF4J 표준 Logger만 사용합니다.
	- structured logging은 Biz 서비스에만 필요하며, 공통 로직이나 로그 수집기에는 사용하지 않습니다.
- logback-spring.xml은 Biz 서비스들이 공유하지만, 로그 필드 해석과 저장은 logging-service가 담당합니다.
	- 로그 타입에 따라 수집 후 필터링이 이루어지므로, Biz 서비스의 성능에 영향이 적습니다.
---
## 🧪 테스트
- 단위 테스트 외에, 구조화된 로그가 정확히 출력되는지 확인하십시오.