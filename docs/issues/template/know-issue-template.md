<%*
const issueOptions = [
  { label: "문서화 전략 — 문서 구조, 관리 정책, 문서 도구 관련 이슈", value: "문서화 전략" },
  { label: "운영 환경 — 개발/배포/테스트 환경 전반의 인프라 구성 이슈", value: "운영 환경" },
  { label: "프로젝트 연관 이슈 — 기술적 문제 또는 외부 요소", value: "프로젝트 연관 이슈" },
];
const labels = issueOptions.map(o => o.label);
const values = issueOptions.map(o => o.value);
const selectedType = await tp.system.suggester(labels, values);

const title = await tp.system.prompt("이슈 제목을 입력하세요 (예: 로컬 환경 포트 충돌)");
const date = tp.date.now("YYYY-MM-DD");
await tp.file.move(`issues/${title}.md`);

// 템플릿 결과 생성
let r = "";
r += `# 📋 ${date} | ${title}\n\n`;
r += `---\n\n`;
r += `- type: \`${selectedType}\`\n`;

if (selectedType === "문서화 전략") {
  r += `  - description: 문서 구조, 관리 정책, 문서 도구 관련 이슈\n`
  r += `    - ex) Obsidian 도입, \`docs\` Vault 분리 등\n\n`;
} else if (selectedType === "운영 환경") {
  r += `  - description: 개발/배포/테스트 환경 전반의 인프라 구성 이슈\n`
  r += `    - ex) 로컬 실행 시 오류, CI/CD 구성 충돌, 포트 충돌 등\n\n`;
} else if (selectedType === "프로젝트 연관 이슈") {
  r += `  - description: 프로젝트 전반에 걸친 기술적 문제 또는 아키텍처적 설계 외부 요소\n`
  r += `    - ex) 테스트 프레임워크 경고, 디펜던시 호환성 등\n\n`;
}

r += `\n---\n\n`;
r += `## 전체 개요\n\n`;
r += `- **발생 일자:** ${date}\n`;
r += `- **제목:** ${title}\n`;
r += `- **상세 내용:**\n`;
r += `  - 어느 형태의 문제인지 간단 메뉴\n`;
r += `  - 어느 단계에서 가장 많이 발생하는지\n`;
r += `  - 현재 리소스에 무시해도 되는지, 결국 처리해야 하는지\n`;
r += `- **현재 상태:**\n`;
r += `  - 무시해도 되는지, 임시 대응이 가능한지, 또는 특정 조치가 필요한지\n`;
r += `- **대응 방안:** (Optional)\n`;
r += `  - 어느 것을 수정해야 할지\n`;
r += `  - 최고의 패치를 기준으로 대응 계획 설정\n`;
r += `- **참고 링크:** (Optional)\n`;
r += `  - [공식 문서나 Stackoverflow 같은 참고 경로]\n`;

r += `\n---\n\n`;
r += `## 🚨 업그레이드 주의사항 (Upgrade Cautions)\n\n`;
r += `- **업그레이드시 신경써야하는 대상**\n`;
r += `  - 어떤 라이브러리(Spring Boot, Gradle, JDK 등)의 버전 업그레이드에 의해 영향을 받을 수 있는지 명시한다.\n`;
r += `- **업그레이드시 발생 가능한 문제**\n`;
r += `  - 기능 제거, 비호환, 설정 깨짐 등\n`;
r += `- **업그레이드시 신경 써야 하는 부분**\n`;
r += `  - 추가 설정, 테스트 필요, 대체 API 적용 등\n`;

r += `\n---\n\n`;

tR += r;
%>
