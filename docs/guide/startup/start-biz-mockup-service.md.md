# 🚀 Biz 모듈 (Mockup Service) 기동 가이드

## 📌 개요
Mockup Service는 비즈니스 로그 이벤트를 발생시키는 테스트용 서비스입니다.  
Kafka를 통해 로그 수집 파이프라인으로 연계되며, 로깅 포맷과 MDC 설정을 기반으로 구조화된 로그를 출력합니다.

---

## ⚙️ 필수 실행 설정

### 🔹 1. Java 옵션
- 반드시 다음과 같은 시스템 프로퍼티를 `-D` 옵션으로 전달해야 합니다:

```bash
-Dservice.name=mockup-service
```
### 🔹 2. 컨테이너 환경변수 옵션
- 반드시 아래 환경 변수를 설정해야 합니다.

| 변수명         | 설명                                            |
| ----------- | --------------------------------------------- |
| `HOST`      | 실행되는 호스트명을 명시 (없을 경우 `unknown-host`로 처리됨)     |
| `CONTAINER` | 컨테이너(Pod) 식별자. 로그의 주요 식별 요소이므로 **반드시 지정**해야 함 |
- Docker / Docker-Compose
```
environment:
  - CONTAINER=mockup-container
```
- Kubernetes
```
env:
  - name: CONTAINER
    valueFrom:
      fieldRef:
        fieldPath: metadata.name
```
>❗ `CONTAINER` 값이 설정되지 않으면 로그 식별이 불가능해지므로 모든 환경에서 반드시 주입해야 합니다.
