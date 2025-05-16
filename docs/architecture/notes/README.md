# 아키텍처 구조 선택 기록

- `2025-05-16-prometheus-wrapping-vs-thanos-cortex.md`  
  Wrapping 구조 도입 후 Thanos/Cortex와의 비교 및 저장소 제한 검토

- `2025-05-16-prometheus-wrapping-vs-direct-kafka-write.md`  
  Prometheus 직접 Kafka 전송 구조와 Wrapping 구조의 정책 통제력 비교

> 이 두 문서는 모두 다음의 설계 원칙을 지키기 위한 구조적 판단을 기록한 것이다:
> 1. 변경 사유 발생 시, **변경 범위를 최소화**할 수 있어야 한다.
> 2. 핵심 구성요소는 **외부 기술에 대한 의존을 최소화하고, 대체 가능성을 고려한 느슨한 결합 구조로 설계되어야 한다.**

---
