---
title: "remove-meta-and-use-dot-notation"
date: "2025-05-29"
status: "done" # [in-progress|done|canceled]
---

# 📝 사고 및 결정 사항 기록

---

## 0. 결정 여부

- ✅ 적용 완료
    - 작업자 : 이리희
    - 완료 일자 : 2025-05-29
    - 작성자 : 이리희
    - 참석자 : 이리희
    - 관련 문서 : n/a

---

## 1. 주제(Title)

`meta` 필드 제거 및 dot notation 기반 필드 네이밍 방식 도입

---

## 2. 문제 인식(Problem Recognition)

초기 구조화 로그 설계에서 meta라는 공통 필드를 만들어 내부에 다양한 키-값을 JSON 형태로 포함시켰으나 다음과 같은 문제가 발생했다:
- 실제로는 Kibana 등의 시각화 툴이 있으므로 meta 필드가 주는 시각적 이점은 거의 없음
- meta 필드 내부가 JSON 문자열이기 때문에 필터링, 쿼리, 색인 등에 비효율적
- 확장성을 위한 필드라고 보기엔, 필드명이 자유롭고 표준화되지 않아 혼란을 야기하며, 코드로 작성할 시에 String으로 변환해야 하기에 너무 불편함

---

## 3. 고려사항(Considerations)

- **Option 1: 기존 meta 필드 유지**
    - 장점: 개발자가 임의의 정보를 빠르게 넣을 수 있음
    - 단점: JSON 문자열 구조로 분석 도구와 통합 시 불편함. 쿼리와 필터링 불가. 일관성 부족

- **Option 2: meta 필드 제거 + dot notation 필드 네이밍 도입**
    - 장점: 명시적인 필드화로 필터링 및 검색 용이. Kibana에서 그룹핑, 검색이 쉬워짐. 표준화 가능
    - 단점: prefix 관리가 필요하며, 초기에 약간의 교육/가이드 제공 필요

---

## 4. 최종 결정(Final Decision)

**Option 2**를 채택한다.  
`meta` 필드를 제거하고, 필요한 확장 필드는 `prefix.subKey` 형태의 dot notation으로 선언하여, 명시적인 구조화 로그 필드로 관리한다.

예:
```json
"user.id": "user-001",
"user.role": "ADMIN",
"calltime.start": "2025-05-28T10:00:00",
"calltime.end": "2025-05-28T10:00:03"
```

---

## 5. 기대효과(Expected Benefits)

- Kibana, Grafana 등에서 **필드 검색, 그룹핑, 필터링이 쉬워짐**
- meta 내부 JSON 파싱 불필요 → **성능 향상 및 쿼리 간결화**
- 명확한 로그 필드 구조 제공 → **타 개발자 협업 시 추적성 향상**
- 정책 표준화 및 문서화 가능

---

## 6. 계속 고민할 사항(Still Open Issues)

- dot notation 필드의 prefix 정책(`user`, `calltime`, `payment` 등)을 명확히 정의할 필요
- Enum 또는 상수화 여부 결정

---

# ✨ 추가 확장 항목 (Optional)

## 대안 방안(Alternative Options)

- meta 유지 + 내부 표준화 노력
- meta 내부를 JsonNode로 파싱해 Kibana에서 가시성 확보 시도

## 리스크 및 대응(Risks & Mitigation)

- 필드 표준화가 되지 않을 경우 혼란 → **Enum 또는 정책 문서로 대응 예정**
- 기존 로그 분석 툴에서 meta 기반으로 시각화된 부분이 있다면 일시적 혼선 발생 가능 → **전면 교체 전 공지 및 문서화**

## 추후 개선 방향(Future Improvements)

- prefix 네이밍 정책 고도화 (`domain.subdomain.key`)
- MDC 자동 주입 도구에서 dot notation 지원 구조로 개선

---

# 📚 작성 규칙

- 문장은 간결하고 명확하게.
- 하나의 문장에는 하나의 의미만.
- 실제 생각의 흐름에 가깝게 기술할 것.

---
