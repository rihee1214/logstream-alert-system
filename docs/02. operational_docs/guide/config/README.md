# config/

> 이 디렉토리는 **서비스 전반에서 사용하는 설정 관련 문서**를 포함합니다.  
> 개발, 테스트, 운영에 필요한 구성 요소에 대한 설명을 문서화한 공간입니다.

---
## 📄 문서 개요

- `common-config.md`:  
  모든 서비스에서 공통적으로 사용하는 설정 항목을 정리한 문서입니다.  
  주로 **biz-service 개발자**가 참고해야 하며, 필수 설정 값들을 포함합니다.

- `mockup-properties.md`:  
  `mockup-service` 실행 시 필요한 테스트용 설정 값을 정의한 문서입니다.  
  운영 환경에서 테스트를 수행하거나, 로컬 개발 시 참조됩니다.

### 📘 설정 정책 해설 (우선순위 및 위치 기준)

이 프로젝트는 설정 값을 다음 우선순위에 따라 관리합니다:

1. **자바 옵션 (`-Dkey=value`)**
   - 런타임 시 가장 높은 우선순위
   - 주로 민감 정보, 컨테이너 환경에서 주입할 항목에 사용

2. **환경 변수**
   - 운영/배포 환경에서 외부 시스템에 의해 설정되는 항목
   - `docker-compose`, `Kubernetes ConfigMap`, `CI/CD`에서 활용

3. **application.properties / application.yml**
   - 서비스 내부 기본값 제공
   - 공통 설정, 디폴트 값 설정 시 사용

> 설정 충돌 시, 우선순위가 높은 쪽이 적용됩니다.

### ✅ 설정 항목 권장 위치

| 항목 유형                 | 권장 위치                    | 예시                      |
| --------------------- | ------------------------ | ----------------------- |
| 민감 정보 (토큰, 비밀번호 등)    | 자바 옵션 또는 환경 변수           | `-Dauth.token=...`      |
| 개발/테스트용 기본값 설정        | `application.properties` | `mockup.enabled=true`   |
| 로깅, metrics, 기타 기술 설정 | `etc/` 문서로 분리하여 관리       | `logback-spring.md`     |
| 모든 서비스에서 공통으로 참조되는 설정 | `common-config.md`       | `service.name`, `env` 등 |

---

## 📁 폴더 개요

- `etc/`  
  자바 옵션, 환경 변수, `application.properties` 외의 **보조 설정 및 기술 특화 설정**을 문서화한 폴더입니다.
	- `logback-spring.md`  
	Spring Boot 환경에서 사용하는 `logback-spring.xml` 설정 구조를 설명합니다.  
	로깅 정책(레벨별 출력 기준, stacktrace 제어 등)에 대한 상세 내용이 포함됩니다.

---

## 🔎 기타

- 공통 설정 항목은 대부분 Spring Boot `application.properties` 또는 환경 변수로 주입됩니다.
- 일부 문서는 configMap, Docker 환경 변수 등 외부 시스템 연계에 사용될 수 있습니다.