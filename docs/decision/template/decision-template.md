<%*
const statusOptions = ["in-progress", "done", "canceled"];
const statusLabels = {
  "in-progress": "🔁 적용 예정",
  "done": "✅ 적용 완료",
  "canceled": "❌ 결정 취소"
};
const status = await tp.system.suggester(
  Object.values(statusLabels),
  Object.keys(statusLabels)
);

const title = await tp.system.prompt("주제를 입력하세요 (예: add-parent-span-id)");
const date = tp.date.now("YYYY-MM-DD");
const fileName = `{${date}}-${title}.md`;
await tp.file.move(`decision/${fileName}`);

const label = statusLabels[status];

// 최종 결과 작성
let result = "";
result += `---\n`;
result += `title: "${title}"\n`;
result += `date: "${date}"\n`;
result += `status: "${status}" # [in-progress|done|canceled]\n`;
result += `---\n\n`;

result += `# 📝 사고 및 결정 사항 기록\n\n`;

result += `---\n\n`;
result += `## 0. 결정 여부\n\n`;

// 라벨 블록만 별도 생성
let labelBlock = `<!-- label-start -->\n`;
labelBlock += `- ${label}\n`;
labelBlock += `<!-- label-end -->`;

// 본문에 labelBlock 넣기
result += labelBlock + "\n";
if (status === "canceled") {
  result += `    - 취소 일자 : YYYY-MM-DD\n`;
  result += `    - 대상 문서 : \\`[취소 후 결정 문서명](경로)\\`\n`;
  result += `    - 취소 사유\n`;
  result += `        - 취소 사유 1: \n`;
  result += `        - 취소 사유 2: \n`;
} else if (status === "in-progress") {
  result += `    - 작업자 : 이름 \n`;
  result += `    - 작업 완료 예정일 : YYYY-MM-DD (or ASAP) \n`;
} else if (status === "done") {
  result += `    - 작업자 : 이름 \n`;
  result += `    - 완료 일자 : YYYY-MM-DD \n`;
}
result += `    - 작성자 : 이름\n`;
result += `    - 참석자 : 이름1, 이름2\n`;
result += `    - 관련 문서 : \\`[관련 문서명](경로)\\` \n`;

result += `\n_(해당 문서의 결정 사항이 실제 코드에 반영되었는지, 혹은 그에 준하는 행위가 이루어졌는지 표시)_\n`;

result += `\n---\n\n`;
result += `## 1. 주제(Title)\n\n_(이슈 혹은 고민 주제를 간단명료하게 적는다)_\n\n`;
result += `---\n\n`;
result += `## 2. 문제 인시(Problem Recognition)\n\n_(왜 이 고민이 발생했는지 배경을 간략히 설명한다)_\n\n`;
result += `---\n\n`;
result += `## 3. 고려사항(Considerations)\n\n`;
result += `- **Option 1: [옵션 이름]**\n  - 장점\n  - 단점\n\n`;
result += `- **Option 2: [옵션 이름]**\n  - 장점\n  - 단점\n\n`;
result += `_(필요시 Option 3, 4 추가)_\n\n`;
result += `---\n\n`;
result += `## 4. 최종 결정(Final Decision)\n\n_(선택한 옵션과 선택 이유를 명확하게 적는다)_\n\n`;
result += `---\n\n`;
result += `## 5. 기대효과(Expected Benefits)\n\n_(이 결정을 통해 기대하는 긍정적 효과를 적는다)_\n\n`;
result += `---\n\n`;
result += `## 6. 계속 고민할 사항(Still Open Issues)\n\n_(아직 확정되지 않았고 추가 검토/구현이 필요한 사항을 정리한다)_\n\n`;
result += `---\n\n`;
result += `# ✨ 추가 확장 항목 (Optional)\n\n`;
result += `## 관련 코드(Linked Code)\n\n_(관련 소스파일, 클래스명을 정리한다)_\n\n`;
result += `| 모듈(Module) | 소스 경로(Source Path) | 클래스명 (Package 포함) | 비고 |\n`;
result += `|--------------|------------------------|--------------------------|------|\n`;
result += `|              |                        |                          |      |\n\n`;

result += `## 대안 방안(Alternative Options)\n\n_(버린 대안들과 버린 이유를 적는다)_\n\n`;
result += `## 리스크 및 대응(Risks & Mitigation)\n\n_(이 결정으로 발생할 수 있는 문제와 그 대응 방안을 적는다)_\n\n`;
result += `## 추후 개정 방향(Future Improvements)\n\n_(나중에 더 발전시킬 수 있는 부분이 있다면 적는다)_\n\n`;
result += `---\n\n`;
result += `# 📚 작성 규칙\n\n`;
result += `- 문장은 간결하고 명확하게.\n`;
result += `- 하나의 문장에는 하나의 의미만.\n`;
result += `- 실제 생각의 흐름에 가깝게 기술할 것.\n\n`;
result += `---`;

tR += result;
%>
