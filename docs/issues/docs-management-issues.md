# 📋 2025-04-30 | 문서 관리 문제

---

- **type: 문서화 전략**

---

## 전체 개요

- **발생 일자:** 2025-04-30
- **제목:** IntelliJ에서 docs 디렉토리 관리 불편 및 Obsidian 도입 결정
- **상세 내용:**
  - 프로젝트 내 `docs/` 디렉토리를 IntelliJ에서 관리하려 하였으나, 문서가 많아짐에 따라서 찾고, 수정하는데 많은 애로사항 발생.
    - 문서 관리 특성상 시각화, 링크 탐색, 메타태그 관리가 필요한데, 이는 IntelliJ보다는 Obsidian이 적합함
- **현재 상태:**
  - `docs/`를 Obsidian 전용 Vault로 분리, IntelliJ에서는 열람만 가능하도록 운영
- **대응 방안:**
  - `docs/README.md`에 Obsidian Vault 사용 및 운영 방식 기술
  - `docs/.obsidian/` 폴더는 `.gitignore`로 관리
- **참고 링크:**
  - [Obsidian 공식 문서](https://help.obsidian.md/)

## 🚨업그레이드 주의사항

- **업그레이드시 신경써야하는 대상**
  - Obsidian 플러그인 버전 호환성
  - Markdown 파일 내 메타블록 형식 변경 가능성

- **업그레이드시 발생 가능한 문제**
  - Obsidian 내부 링크 (`[[...]]`)가 다른 뷰어에서 동작하지 않을 수 있음
  - Vault 외부에서 파일 이동 시 링크 깨짐 우려

- **업그레이드시 신경 써야 하는 부분**
  - `.md` 파일은 GitHub 렌더링을 고려해 Obsidian 전용 마크업은 제한적으로 사용
