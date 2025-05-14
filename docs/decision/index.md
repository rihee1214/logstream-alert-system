> ⚠️ 이 문서는 Obsidian에서 자동 요약을 위한 인덱스입니다. 직접 편집하지 마세요.
```dataviewjs
const status = (p) => {
  if (p.path.includes("/done/")) return "✅ done";
  if (p.path.includes("/canceled/")) return "❌ canceled";
  if (p.path.includes("/deferred/")) return "⏸️ deffered";
  return "🔁 in-progress";
};

dv.table(
  ["제목", "상태", "생성일"],
  dv.pages('"decision"')
    .where(p => !p.file.name.includes("index") &&
				!p.file.path.includes("/template/")
				)
    .sort(p => p.date, "desc")
    .map(p => [p.file.link, status(p.file), p.date])
);
```

✅ 이 테이블은 다음을 자동으로 수행합니다:

- `my-topic/` 이하 전체 폴더 스캔
- `/done/` 포함된 파일은 `done`
- `/canceled/` 포함된 파일은 `canceled`
- `/deferred/` 포함된 파일은 보류 파일 `deffered`
- 나머지는 `in-progress`
- `index.md` 파일은 제외
- 제목, 상태, 경로 컬럼 출력

---

## ✅ 추가 옵션 (예)

- 상태별 색상 강조 (`dataviewjs`로 확장 가능)
- `file.link`로 클릭 가능한 링크 표시
- `created`, `updated` 날짜 추가
