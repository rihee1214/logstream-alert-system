# 📝 사고 및 결정 사항 기록 템플릿 (Blank Template)

---

## 0. 결정 여부

_(해당 문서의 결정 사항이 실제 코드에 반영이 되었는지, 혹은 그에 준하는 행위가 이루어 졌는지 표시)_
- 🔁 적용 예정 (필요시 적용 예정 날짜, 적용 할 사람 기재)
  - 작업자 : 이름 
  - 작업 완료 예정일 : YYYY-MM-DD (or ASAP)
  - 작성자 : 이름
  - 참석자 : 이름1, 이름2
  - 관련 문서 : `[관련 문서명](경로)`
- ✅ 적용 완료
  - 작업자 : 이름
  - 완료 일자 : YYYY-MM-DD
  - 작성자 : 이름
  - 참석자 : 이름1, 이름2
  - 관련 문서 : `[관련 문서명](경로)`
- ❌ 결정 취소
  - 취소 일자 : YYYY-MM-DD
  - 대상 문서 : `[관련 문서명](경로)`
  - 취소 사유
	  - 사유 1
	  - 사유 2
  - 작성자 : 이름
  - 참석자 : 이름1, 이름2
  - 관련 문서 : `[관련 문서명](경로)`

---

## 1. 주제(Title)

_(이슈 혹은 고민 주제를 간단명료하게 적는다)_

---

## 2. 문제 인시(Problem Recognition)

_(왜 이 고민이 발생했는지 배경을 가락히 설명한다)_

---

## 3. 고려사항(Considerations)

- **Option 1: [옵션 이름]**
    - 장점
    - 단점

- **Option 2: [옵션 이름]**
    - 장점
    - 단점

_(필요시 Option 3, 4 추가)_

---

## 4. 최종 결정(Final Decision)

_(선택한 옵션과 선택 이유를 명확하게 적다)_

---

## 5. 기대효과(Expected Benefits)

_(이 결정을 통해 기대하는 강정적 효과를 적다)_

---

## 6. 계속 고민할 사항(Still Open Issues)

_(아직 확정되지 않았고 추가 검토/구현이 필요한 사항을 정리한다)_

___

# ✨ 추가 확장 항목 (Optional)

## 관련 코드(Linked Code)

_(관련 소스파일, 클래스명을 정리한다)_

| 모듈(Module) | 소스 경로(Source Path) | 클래스명 (Package 포함)                                                 | 비고             |
|------------|--------------------|-------------------------------------------------------------------|----------------|
| common     | src/main/java      | com.rihee.alerting.common.log.annotation.StructuredRestController | 기존 방식 폐기       |
| common     | src/main/java      | com.rihee.alerting.common.log.annotation.StructuredRequestMapping | 새로운 Annotation |
| common     | src/main/java      | com.rihee.alerting.common.log.annotation.StructuredGetMapping     | 새로운 Annotation |
| common     | src/main/java      | com.rihee.alerting.common.log.annotation.StructuredPostMapping    | 새로운 Annotation |
| common     | src/main/java      | com.rihee.alerting.common.log.annotation.StructuredPutMapping     | 새로운 Annotation |
| common     | src/main/java      | com.rihee.alerting.common.log.annotation.StructuredDeleteMapping  | 새로운 Annotation |
| common     | src/main/java      | com.rihee.alerting.common.log.annotation.StructuredPatchMapping   | 새로운 Annotation |

## 대안 방안(Alternative Options)

_(버린 대안들과 버린 이유를 적다)_

## 리스크 및 대응(Risks & Mitigation)

_(이 결정으로 발생할 수 있는 문제와 그 대응 방안을 적다)_

## 추후 개정 방향(Future Improvements)

_(나중에 더 발전시키어 줄 수 있는 부분이 있다면 적다)_

---

# 📚 작성 규칙

- 문장은 간결하고 명확하게.
- 하나의 문장에는 하나의 의미만.
- 실제 생각의 흐름에 가깝게 기술할 것.

---

# 📁 파일 저장 위치와 이름 귀칙

- 경로: `decision/`
- 파일명: `{YYYY-MM-DD}-간단한주제.md`
    - 예시: `{2025-04-26}-mock-log-validation.md`