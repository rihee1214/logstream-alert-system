# Issues & Decisions Notes

> 이 디렉토리는 개발 중 마주친 **이슈 및 전략적 선택 사항**들을 기록하는 공간입니다.  
> 문제 해결의 맥락을 보존하고, 유사 상황 발생 시 참고 자료로 삼기 위해 체계적으로 관리됩니다.

---

## 디렉토리 구조
```
issues/  
├── template/ # 이슈 문서 및 전략 문서 템플릿  
│ └── decision-template-humanreadable.md  
├── mvp-strategy.md # MVP 전략 설정 문서  
├── tooling-obsidian.md # Obsidian을 문서 관리 도구로 채택한 배경  
├── mockito-troubles.md # Mockito에서 발생한 문제 및 원인 정리  
└── ... (기타 이슈 및 결정 사항 문서)
```
---

## 구성 설명

- **`template/`**  
  새로운 이슈 문서를 생성할 때 참고할 수 있는 **템플릿 문서**를 제공합니다.  
  - `decision-template-humanreadable.md`: 사람이 읽기 좋은 형태의 이슈/결정 템플릿

- **이슈 문서**  
  개발 중 직면한 문제들, 고민의 흔적, 선택한 방향, 기술적 절충안 등을 담습니다.  
  - 예: `mvp-strategy.md`는 최소 기능 제품 정의 기준을,  
         `tooling-obsidian.md`는 문서 도구 선택 배경을 기술합니다.

---

## 작성 지침

- 이 디렉토리에는 **구체적인 기능 이슈**보다는,  
  _전반적인 전략 결정_, _개발 흐름 상 반복 가능성이 높은 고민_,  
  _프로젝트 관리상의 철학/선택_ 들을 기록하는 것이 좋습니다.
  
- 단순 버그나 TODO는 코드 내 주석이나 issue tracker (ex: GitHub Issues)로 대체하고,  
  이 디렉토리는 **깊이 있는 사고의 결과물** 중심으로 유지합니다.

---

## 참고

- 아키텍처 관련 결정은 `architecture/notes/`에,  
  세부 아키텍처 관련 결정은 decision에 문서화하는 것을 권장합니다.

