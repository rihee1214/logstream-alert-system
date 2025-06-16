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

---
## 결론

### 필수 컴포넌트

- **Sidecar** :  prometheus 에 sidecar로 붙어서 prometheus가 수집한 metric을 모두 외부로 전달하고 실시간 상태를 노출한다. Prometheus가 저장한 TSDB block을 일정 주기로 객체 저장소에 업로드한다.
- **compactor** : 객체 저장소에 저장되어있는 블록 데이터의 중복을 제거하고, 압축 시키는 역할을 한다.
- **storegateway** : long-term storage 상의 메트릭 조회를 담당함 (최근 데이터는 Sidecar가 처리)
- **query** : Sidecar로부터 최근 실시간 메트릭을, Store Gateway로부터 장기 보존된 메트릭을 수집하여 Grafana 등에서 PromQL로 단일하게 조회할 수 있도록 처리함

### 추후 추가 가능 컴포넌트

- **query frontend** 
	- Query의 기능을 향상 시키기 위한 캐시 컴포넌트
	- 추후 Query의 성능이 떨어지면 추가 하면 됨
- **ruler**
	- 각 서비스 별 metric 알람 규칙을 지정하기 위한 컴포넌트
	- Prometheus 내 개별 alert rule을 계속 유지할지, Ruler 기반으로 중앙에서 통합 관리할지를 명확히 정의한 뒤 도입할 것
- 기타(Bucket Web, Tools)
	- Compactor가 제대로 동작하는지 확인하거나, 블록을 직접 확인하기 위해서 필요한 툴이다.
	- 해당 요소도 있으면 좋은 보조 도구 이기 때문에 필요 시 사용하는 것이 좋음
### 추후 고려사항

Ruler를 사용하기로 결정했다면 prometheus 규칙은 어떻게 들고 갈지. Ruler 규칙은 어떻게 들고 갈지 결정해야 한다.
**결국 두 규칙은 서로 잘 분리되어 있어야 추후 어디에 어떤 것을 두었는지 찾을 필요가 없어진다.**

### 🔧 Ruler 정책에 대한 기본 마인드

1. **상호 호출이 많은 서비스들을 지역적으로 묶는다.**  
    (예: 주문 시스템, 결제 시스템, 상품 시스템이 동일 리전/노드에 존재)   
2. **지역 단위로 Prometheus 군집을 구성하여 metric을 수집한다.**
    - Prometheus는 지역 내 서비스들의 공통 메트릭 수집을 담당
    - 예: node_exporter, service_exporter, HTTP 500 등
3. **지역 Prometheus에서 해당 서비스군의 공통 alert rule을 지정하고 적용한다.**
    - 즉, **지역 인프라 상태, 공통 성능 이슈 등은 Prometheus에서 바로 알람**
4. **Ruler는 전체 Prometheus Sidecar로부터 데이터를 수신하며, 서비스별로 개별 Alert Rule을 지정한다.**
    - 즉, **서비스 특화 메트릭(비즈니스 로직 기반)은 Ruler에서 알람 정의 및 전송**