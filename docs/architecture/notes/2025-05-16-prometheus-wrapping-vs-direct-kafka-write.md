# Prometheus 직접 Kafka 전송 구조와 Wrapping 구조 비교 검토

---
> 이 구조는 이미 Wrapping 기반으로 구성되어 있고, 실시간 정책 판단, fallback 제어, 저장소 선택 등 모든 흐름의 중심이 Wrapping에 집중되어 있다.
> 
> 이 문서는 Wrapping 구조를 이미 도입한 상황에서, "Prometheus가 직접 Kafka로 보내는 구조가 더 나았을까?"라는 의문을 바탕으로 비교 분석하였다.
> 결과적으로 Wrapping 구조가 가져다주는 정책 제어와 운영 유연성의 강점을 다시 확인하기 위한 기록이다.
---

## 1. 의문

Wrapping Service 없이 Prometheus가 직접 Kafka로 메트릭/알림 데이터를 전송하고,  
이후 처리(write to Elasticsearch, trigger alerts 등)는 별도 consumer에서 수행하는 구조로 전환할 경우,  
현재 Wrapping 구조와 비교해 어떤 장단점과 아키텍처적 판단 차이가 발생하는가?

## 2. Prometheus 직접 Kafka 전송 구조 요약

- 수집: Prometheus → remote_write adapter 또는 exporter → Kafka
- 처리: Kafka consumer가 Elastic 저장 또는 알림 트리거 수행
- 장점:
  - Prometheus만으로 수집/전송 기능 구성 가능
  - Wrapping Service 제거로 구조 간소화 가능
- 한계:
  - Prometheus는 policy 판단, dedup, fallback 등 **로직을 가질 수 없음**
  - 저장/알림 처리를 위한 **별도 write processor, alert trigger consumer 등 복수 컴포넌트 필요**
  - 알림 흐름, 저장 흐름, 정책 흐름이 분산되어 **가시성 및 통제 어려움**

## 3. Wrapping 구조 선택 이유

- 모든 수집된 메트릭은 Wrapping에서 통합 수신하여 저장, 알림, 정책 적용을 한 곳에서 수행 가능
- Kafka consumer를 추가로 운영하지 않아도 되고, Kafka를 **완전한 transport 계층**으로만 사용
- 장애 대응, 정책 분기, dedup 처리, fallback 설정 등 **모든 흐름을 Wrapping 내에서 통제 가능**
- 구조적으로 단일 진입점으로 구성되므로, **관찰성과 추적성이 높음**

## 4. 결론

Prometheus → Kafka → 다중 consumer로 이어지는 구조는  
구성은 간단하지만 결국 **분산된 처리 책임으로 인해 운영 복잡도와 제어력 부족**이라는 문제를 초래한다.  
특히 정책 기반의 알림 판단(예: 5회 이상 실패 시 전송)이나 fallback 조건 설정이 필요한 경우,  
Prometheus 단독 구조는 이러한 판단 로직을 가질 수 없어, 결국 별도의 "Write Logic Processor" 계층이 필요하다.

반면 Wrapping 구조는 수집 이후의 모든 흐름(저장, 알림, 정책 판단)을 하나의 컴포넌트에 집중시켜  
운영의 명확성과 통제 가능성을 크게 향상시킨다.

따라서 본 시스템에서는 Wrapping을 유지하고, Prometheus는 단순 수집기로 제한하는 구조가  
정책적 유연성과 장애 대응력을 유지하는 데 더 적합하다고 판단하였다.
