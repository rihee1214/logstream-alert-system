# Thanos 주요 Component 선택 결정

Thanos는 여러가지 Component의 조합으로 이루어져 있기 때문에 그 조합을 이용하여 완전한 솔루션을 구축해야한다. 그렇기 때문에 각 컴포넌트에 대한 개요와 어떤 것을 사용할지, 어떤 구성을 가지고 가야 할지 고민해야한다.
## ✅ Thanos의 주요 컴포넌트 목록 및 간단한 설명

| 컴포넌트               | 설명                                                  | 필수 여부                                 |
| ------------------ | --------------------------------------------------- | ------------------------------------- |
| **Sidecar**        | Prometheus에 붙어서 데이터를 Thanos Store로 전달하고, 쿼리할 수 있게 함 | ✅ 반드시 필요 (모든 Prometheus 인스턴스에 1:1 대응) |
| **Store Gateway**  | S3/GCS 등 오브젝트 스토리지에 저장된 long-term 데이터를 불러오는 역할      | ✅ 장기 보존 사용 시 필수                       |
| **Query**          | 여러 데이터 소스를 통합해서 단일 쿼리 엔트리포인트 제공                     | ✅ 대부분의 구성에서 필수                        |
| **Query Frontend** | 쿼리를 캐싱/분할/병렬화해서 성능 최적화                              | ❌ 성능 튜닝이 필요할 때 사용                     |
| **Compactor**      | 오브젝트 스토리지 내 블록을 압축하고 retention을 정리                  | ✅ long-term 저장 시 필수                   |
| **Ruler**          | Prometheus처럼 alert rule을 평가하고 알람 발생                 | ❌ 필요 시 사용 (별도의 룰 처리 목적)               |
| **Receiver**       | 수집만 전담하는 component (모든 WAL 수신) — TSDB ingestion 전용  | ❌ Prometheus 대신 쓰는 구조에서만 사용           |

- SideCar는 무조건 필수로 들어가야 한다. 
  각 Prometheus 인스턴스에 붙어서 메트릭 데이터를 외부로 전달하고, 실시간 상태를 Querier에 노출하기 때문이다.
- StoreGateway도 필수다.
  객체 저장소에 저장된 장기 데이터를 Querier가 조회할 수 있도록 함 → 트렌드 분석, 리포트 용
- Query
  PromQL 쿼리 처리의 핵심. Grafana가 직접 연결되는 엔드포인트
- Compactor
  오래된 데이터를 압축하고 해상도를 낮추는 역할 (retention 및 S3 비용 절감 목적)

- Ruler는 고민 대상이다. 
  Alert Rule을 별도로 실행하는 독립 엔진.  
  Prometheus 내부 Alert과 중복 관리 발생 가능성이 있어, **관리 포인트 분리 기준**을 명확히 할 것
- Query Frontend는 애매하다.
  쿼리 캐싱, 분산 실행, 요청 제한 등 운영 편의성은 높지만  
  **운영 복잡도 추가됨**. 대규모 트래픽/요청량을 대비한 성능 최적화 시 도입 고려

> Sidecar, Store Gateway, Query, Compactor는 거의 **필수 조합**이다.  
> Ruler와 Query Frontend는 **운영 전략과 관리 체계가 명확할 경우에만 도입**하는 것이 바람직하다.  
> 특히 Ruler는 Alert 관리의 복잡도를 유발하므로, **중앙 Alert 관리 체계를 갖춘 뒤** 도입하는 것이 이상적이다.