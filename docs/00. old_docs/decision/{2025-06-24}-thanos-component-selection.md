---
title: "thanos-component-selection"
date: "2025-06-24"
status: "done" # [in-progress|done|canceled]
---

# 📝 사고 및 결정 사항 기록

---

## 0. 결정 여부

- ✅ 적용 완료
    - 작업자 : 이리희
    - 완료 일자 : 2025-06-24
    - 작성자 : 이리희
    - 참석자 : 이리희
    - 관련 문서 : n/a

---

## 1. 주제(Title)

Thanos 구성 시 사용/제외할 주요 컴포넌트 결정

---

## 2. 문제 인식(Problem Recognition)

Thanos는 다양한 컴포넌트로 구성되며, 목적에 따라 유연하게 조합할 수 있는 구조를 갖고 있다.  
하지만 모든 컴포넌트를 한 번에 도입하면 구성 복잡도와 운영 부담이 과도하게 증가한다.

현재는 초기 운영 단계로, **장기 메트릭 보존과 안정적인 쿼리 처리**가 주요 목적이며,  
**알림 정책 분리 및 고급 캐싱 최적화는 추후 고려 대상**이기 때문에,  
도입할 컴포넌트와 향후 확장 예정인 컴포넌트를 명확히 구분하여 결정할 필요가 있다.

---

## 3. 고려사항(Considerations)

- 필수 컴포넌트
	- **Sidecar**: Prometheus로부터 메트릭 전달, TSDB 업로드, 실시간 쿼리 처리
	- **Store Gateway**: S3 등 장기 저장소에 있는 블록 데이터 조회
	- **Compactor**: 블록 압축 및 retention 유지
	- **Query**: Grafana 등의 PromQL 요청을 단일 쿼리 인터페이스로 처리

- 선택 고려 컴포넌트
	- **Query Frontend**
		- 고부하 환경에서 쿼리 병렬화 및 캐싱 처리 가능
		- 초기 운영에는 과도한 복잡도 → 추후 성능 문제 발생 시 도입
	- **Ruler**
		- 비즈니스 알림 정책의 중앙화 처리 목적
		- 현재는 Prometheus 내 Alert Rule 사용 유지 → **알림 관리 정책 정립 이후 도입**
	- **Bucket Web, Tools**
		- Compactor 상태 시각화 및 디버깅 도구
		- 필수는 아니지만, 운영 편의성을 위해 추후 도입 고려

---

## 4. 최종 결정(Final Decision)

### ✅ 초기 도입 컴포넌트

- `Sidecar`: Prometheus 별로 필수 구성
- `Store Gateway`: 장기 보존을 위한 S3 조회 지원
- `Compactor`: Storage 내 메트릭 블록 최적화
- `Query`: Grafana 등의 단일 쿼리 엔트리포인트

### ⏳ 추후 도입 고려 컴포넌트

- `Query Frontend`: 요청량 증가, 성능 병목 발생 시 도입
- `Ruler`: Alert 정책이 분리되고 중앙화될 필요가 생길 경우
- `Bucket Web, Tools`: 추후 블록 확인 필요 시 이용(운영 시 필요할 수도 있음)

---

## 5. 기대효과(Expected Benefits)

- 최소한의 구성으로도 **장기 메트릭 저장**과 **통합 쿼리** 기능 확보
- 도입 초기의 **운영 복잡도 최소화**
- 필요한 시점에 맞춰 **컴포넌트 확장 가능** (Query Frontend, Ruler 등)
- Alert 정책의 분리 시점 판단을 유보함으로써 **설계 유연성 확보**

---

## 6. 계속 고민할 사항(Still Open Issues)

- `Ruler` 도입 시 Alert 정책 분리 기준 정립 필요
    - Prometheus의 지역 단위 알람 vs Ruler의 서비스 특화 알람 구분
- `Query Frontend`의 도입 시기와 도입 후 캐싱 정책 구성
- 각 컴포넌트 간 버전 호환성 관리 및 CI/CD 연동 방안
- Bucket Web 등 시각화 도구의 필요성과 도입 기준 수립

---

# 📚 작성 규칙

- 문장은 간결하고 명확하게.
- 하나의 문장에는 하나의 의미만.
- 실제 생각의 흐름에 가깝게 기술할 것.

---
