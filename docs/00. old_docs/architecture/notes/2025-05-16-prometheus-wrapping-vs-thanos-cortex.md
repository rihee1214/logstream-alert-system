# Prometheus Wrapping 구조와 Thanos / Cortex 비교 검토

---
> 이 구조는 이미 Wrapping 기반으로 구성되어 있으며, 메트릭 저장, 알림 트리거, 정책 판단 등의 모든 흐름을 Wrapping 내부에서 통제하고 있다.  
> 
> 그러나 구조적 단순성을 이유로, Prometheus가 직접 Kafka로 데이터를 전송하고 이후의 처리는 별도 consumer들이 맡는 방식에 대한 검토가 잠시 이루어졌고, 이 문서는 그 과정에서 도출된 구조적/운영적 차이를 정리하고 Wrapping 구조의 타당성을 다시 확인하기 위해 작성되었다.
---

## 1. 의문
Prometheus의 A-A 구조를 Wrapping하고 Alertmanager 및 Elastic 저장을 직접 제어하는 구조를 선택했을 때, 기존 오픈소스 확장 구조들(Thanos, Cortex 등)과 비교하면 어떤 차이가 있는가?

## 2. 오픈소스 확장 구조 요약

### Thanos
- 목적: 장기 저장, 수평 확장, 글로벌 쿼리
- 저장소: S3-compatible Object Storage만 가능
- 한계: 실시간 정책 제어, 자유 저장소 지정 불가

### Cortex
- 목적: 멀티 테넌시, API 호환 Prometheus
- 저장소: Chunk or Block 저장 (DynamoDB 등)
- 한계: Alert 전송, 정책 유연성 제한

## 3. Wrapping 구조 선택 이유

- Prometheus는 pull-only 수집기로 사용
- 저장 및 알림은 Wrapping Service에서 제어
- 저장소는 ElasticSearch 기반으로 운영자 가시성 확보
- Alertmanager fallback 및 deduplication 정책을 직접 구성 가능

## 4. 결론

본 시스템에서는 Wrapping을 유지하고, Prometheus는 단순 수집기로 제한하는 구조가 정책적 유연성과 장애 대응력을 유지하는 데 더 적합하다고 판단하였다.

또한, Wrapping 구조는 아키텍처의 핵심 원칙 중 하나인 **"단일 컴포넌트에서 수집 이후 흐름을 통제할 수 있어야 한다"**는 설계 철학에 부합한다.  
Prometheus에서 직접 Kafka로 전송하는 구조는 단순해 보일 수 있지만, 알림 분기, 저장소 선택, 장애 대응 등의 책임이 분산되며, 결국 전체 흐름에 대한 통제권을 상실하게 만든다.

반면 Wrapping은 하나의 경로에 모든 메트릭 후처리 로직이 집중되므로, 구조적으로도 운영 관점에서도 **명확한 관찰성과 예측 가능한 제어 흐름을 보장한다.**