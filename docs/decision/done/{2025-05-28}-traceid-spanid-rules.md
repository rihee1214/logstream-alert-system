---
title: "traceid-spanid-rules"
date: "2025-05-28"
status: "done" # [in-progress|done|canceled]
---

# 📝 사고 및 결정 사항 기록

---

## 0. 결정 여부

- ✅ 적용 완료
    - 작업자 : 이리희
    - 완료 일자 : 2025-05-28
    - 작성자 : 이리희
    - 참석자 : 이리희
    - 관련 문서 : [bizlog-contract](bizlog-contract.md)

---

## 1. 주제(Title)

traceId / spanId 생성 규칙 및 유효성 검증 정책 확정

---

## 2. 문제 인식(Problem Recognition)

현재 로그 시스템에서 traceId와 spanId는 분산 트레이싱 및 추적의 핵심 식별자 역할을 한다.  
그러나 이들에 대한 **형식적 규칙**과 **유효성 검증 방식**이 시스템 전반에 명확히 정립되지 않아 다음과 같은 문제가 존재한다:

- 외부 서비스에서 전달된 traceId / spanId가 형식 불일치로 인해 로그 연계 실패
- 로그 저장 시 잘린 문자열이 유효값으로 오인될 가능성
- 서비스 간 trace 연계/분석 도구(Grafana, Kibana 등)에서 ID 불일치로 분석 불가

---

## 3. 고려사항(Considerations)

### 📌 생성 규칙

- traceId와 spanId는 모두 **hex 문자열**로 구성한다.
- traceId는 **ThreadLocalRandom 기반 128비트(32자리)** hex 문자열 생성
- spanId는 **ThreadLocalRandom 기반 64비트(16자리)** hex 문자열 생성
- UUID는 사용하지 않으며, 속도와 파싱 효율을 위해 compact hex 방식을 채택함

### 📌 유효성 검증 정책

- traceId는 다음 조건을 만족해야 유효:
    - hex 문자열
    - **길이가 32 이상의 32의 배수 (32, 64, 96 등)**
- spanId는 다음 조건을 만족해야 유효:
    - hex 문자열
    - **길이가 16 이상의 16의 배수 (16, 32, 48 등)**
- 위 조건에 부합하지 않으면 **자동 생성**하여 대체한다

---

## 4. 최종 결정(Final Decision)

- 생성 및 검증 정책은 모두 **StructuredLogInterceptor 내부에 캡슐화**한다
- 외부에서는 생성/검증에 대해 직접 접근하거나 수정할 수 없도록 한다
- traceId / spanId는 **모든 로그 출력의 기본 식별자**로 활용되며, 유효하지 않은 경우 자동 생성으로 대체되며, 기존 요청자는 이를 인지하지 않는다
- 추후 길이 확장 가능성을 고려
	- traceId는 32의 배수
	- spanId는 16의 배수

---

## 5. 기대효과(Expected Benefits)

- 로그 추적의 안정성 및 연속성 보장
- 로그 저장 및 수집 중 발생할 수 있는 오류에 대한 회복력 향상
- 향후 ID 체계 확장 시 구조 변경 없이 수용 가능
- 시스템 전체의 추적 ID 형식 통일로 분석 툴 연계 용이

---

## 6. 계속 고민할 사항(Still Open Issues)

- traceId에 의미 기반 prefix (서비스명 등)를 붙일 경우 정책을 어떻게 확장할지
- Kibana/Elasticsearch와 연동 시 ID 필드에 대한 색인 최적화
- logType 또는 serviceName과 ID 간의 관계 명세 필요 여부

---

# ✨ 추가 확장 항목 (Optional)

## 관련 코드(Linked Code)

| 모듈(Module) | 소스 경로(Source Path) | 클래스명 (Package 포함)                                      | 비고                |
| ---------- | ------------------ | ------------------------------------------------------ | ----------------- |
| common     | src/main/java      | com.rihee.logging.interceptor.StructuredLogInterceptor | 정책 적용 위치          |

## 리스크 및 대응(Risks & Mitigation)

- **리스크**: 타 시스템과의 연동 시 ID 형식 불일치 발생 가능
- **대응**: 유효성 검사를 통해 시스템 진입 지점에서 무효값 차단 및 재생성 처리

## 추후 개선 방향(Future Improvements)

- `traceIdVersion`, `originServiceName`, `hierarchicalId` 등 복합 식별자 구조 도입 시 확장 가능성 열어둠
- 필요 시 ID 생성 방식 자체를 외부 설정 가능하도록 추상화 고려

---

# 📚 작성 규칙

- 문장은 간결하고 명확하게.
- 하나의 문장에는 하나의 의미만.
- 실제 생각의 흐름에 가깝게 기술할 것.

---
