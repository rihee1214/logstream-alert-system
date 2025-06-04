# ⏱ Actuator Scheduler 확장 가이드
>이 문서는 공통 모듈에서 제공하는 Actuator 상태 로깅 스케줄러 외에, 각 서비스에서 자체 목적에 따라 **추가적인 Actuator Call Scheduler를 구성할 수 있는 방법**을 설명합니다.

---

## ✅ 기본 scheduler 동작

공통 모듈은 `ActuatorHealthMonitoringScheduler`를 통해 `/actuator/health` endpoint를  
주기적으로 호출하고 structured log를 출력합니다.

- 주기 설정: `monitoring.scheduler.interval.ms`
- 로그 타입: `logtype=act`
- 출력 방식: `StructuredLogger` 기반 JSON 로그
- 로그 목적: Prometheus 외 상태 이중 기록, 통합 모니터링, 실패 추적 등

---

## 🔧 커스터마이징: 확장 방식

서비스에서 **추가적인 health check 또는 외부 시스템 상태 체크**가 필요하다면,  
다음과 같은 방식으로 **자체 scheduler를 추가**할 수 있습니다.

### 📌 예시

```java
@Component
public class CustomRemoteHealthChecker {

    @Scheduled(fixedDelayString = "30000")
    public void checkExternalService() {
        // 외부 시스템 ping → structured log 출력
    }
}
```

- 공통 StructuredLogger를 그대로 사용하거나,
- Biz 전용 로그 구조로 변형 가능

---
## 🚫 기본 scheduler 끄기 (선택적)

만약 `/actuator/health` 호출이 의미 없거나,  
기본 health 로그를 제거하고 싶다면 다음 설정을 통해 비활성화할 수 있습니다:

```properties
monitoring.scheduler.enabled=false
```

- 해당 설정이 false일 경우, 공통 모듈의 기본 스케줄러는 동작하지 않습니다.

---

## 💡 설계 철학

|항목|설명|
|---|---|
|✅ 확장 중심|기본 scheduler는 그대로 두고, 서비스별로 추가 정의 가능|
|❌ 재정의 구조 지양|공통 구조가 변경되면 유지보수 복잡도 증가|
|✅ 설정 기반 제어|필요 시 `application.properties`로 기본 scheduler 제어 가능|

---

## 📎 관련 문서

- [actuator-logging.md](actuator-logging.md)
- [structured-logging.md](structured-logging.md)
- [structured-log-extension.md](structured-log-extension.md)
