# 🔹 Keyspace

- **역할**: 테이블들을 그룹화하고, 그 그룹 전체에 대한 **복제 전략을 정의**하는 논리 단위.  
- **비유**: 오라클의 계정(schema), MySQL의 database와 유사.
- **내부 구성**: 테이블, 뷰 등 객체들을 포함.
- **주요 기능**: 어떤 datacenter에 몇 개의 replica를 둘 것인지 정의함.
- ✔️ **중요**: 복제 전략은 keyspace 수준에서 설정됨. 테이블마다 설정하는 게 아님.

```cql
CREATE KEYSPACE log_keyspace 
WITH replication = {   
	'class': 'NetworkTopologyStrategy',   
	'dc1': 3 
};
```

---

# 🔹 Datacenter

- **역할**: 여러 노드를 묶는 **물리적 그룹**, 복제를 분산하는 대상.    
- **구성**: 동일 네트워크/지리적 위치 또는 논리적으로 묶인 노드 그룹.
- **비유**: Kubernetes의 Region/Zone 개념과 유사. (예: `us-east-1`, `ap-northeast-2`)
- ✔️ **복제 정책은 datacenter 이름 단위로 정의**되기 때문에, keyspace가 `datacenter 이름`을 정확히 참조해야 함.

---

# 🔹 Contact Point

- **역할**: 클러스터의 진입점.    
- **실무 적용**: 1~2개의 노드만 지정해도 클러스터 전체 정보를 자동으로 수집함.
- ✔️ 클러스터 안의 모든 노드 정보를 자동으로 가져오기 때문에, **전체 노드를 나열할 필요는 없음.**

```properties
scylla.contact-points=node1:9042,node2:9042 scylla.datacenter=dc1 scylla.keyspace=log_keyspace
```

---

# ✅ 결론 (실무 관점 요약)

| 항목                 | 설명                                                                                 |
| ------------------ | ---------------------------------------------------------------------------------- |
| **Keyspace**       | 데이터베이스 단위. 복제 정책을 담고 있음. 복제 대상은 datacenter 이름으로 지정                                 |
| **Datacenter**     | 실제 노드가 있는 물리적 클러스터. 복제본이 배치되는 단위                                                   |
| **Contact Points** | 클러스터 진입용 주소. 최소 1개만 있어도 전체 토폴로지 로딩 가능                                              |
| **접근 방식**          | Contact Point를 통해 진입 → 해당 노드를 통해 클러스터 메타 정보 탐색 → Keyspace 기준으로 datacenter 복제 정책 적용 |
