### 📌 Prometheus / Alertmanager 이중화 전략 고려사항

- **Prometheus**
    - 자체적으로 deduplication 처리를 하지 않음
    - Alert 전달 시 `group_by` 등으로 묶어서 AlertManager에 전송
    - 다중 Prometheus 인스턴스 운영 시, 동일 Alert가 여러 번 전달될 수 있으므로 AlertManager 측에서 중복 제거 필요
        
- **Alertmanager**
    - `--cluster.peer` 옵션을 이용한 **클러스터링 구성**
    - 내부적으로 **fingerprint 기반 deduplication** 및 상태 동기화 (silence, inhibition 등)
    - 앞단에 **로드밸런서**를 두어 Prometheus가 단일 진입점으로 접근하도록 설정

>이 구조를 통해 Prometheus의 고가용성과 Alertmanager의 상태 일관성 및 알림의 중복 제거를 동시에 달성할 수 있음.