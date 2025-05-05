# 📦 Common 모듈 개발 가이드

## 📌 개요
Common 모듈은 로깅, 인터셉터, Actuator 설정 등 모든 서비스가 공유하는 공통 유틸리티를 제공합니다.

## 🔧 개발 시 유의사항
- MDC 설정을 보존해야 하므로, 사용자 정의 Interceptor는 `order > 0`으로 설정해야 합니다.
- `StructuredLoggerFactory`는 Biz 서비스 전용이며, Common 모듈 내부에서는 직접 사용하지 않습니다.
- logback-spring.xml 설정은 모든 Biz 서비스가 공통으로 사용하되, 필드 분기 및 저장은 logging-service가 수행합니다.

## 🧪 테스트
- 단위 테스트 외에도, MDC 필드(`traceId`, `spanId`, `logtype`, 등)가 정상적으로 출력되는지 확인하십시오.