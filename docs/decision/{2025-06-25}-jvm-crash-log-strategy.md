---
title: "jvm-crash-log-strategy"
date: "2025-06-25"
status: "in-progress" # [in-progress|done|canceled]
---

# 📝 사고 및 결정 사항 기록

---

## 0. 결정 여부

- 🔁 적용 예정
    - 작업자 : 이리희
    - 작업 완료 예정일 : ASAP
    - 작성자 : 이리희
    - 참석자 : 이리희
    - 관련 문서 : n/a

---

## 1. 주제(Title)

JVM Crash 발생 시 로그 및 Core Dump 저장 전략 수립

---

## 2. 문제 인식(Problem Recognition)

컨테이너 환경에서 Java 애플리케이션이 OOM(OutOfMemoryError) 또는 기타 치명적인 오류로 인해 강제 종료될 경우,  
JVM은 `hs_err_pid.log` 또는 core dump를 생성하지만, **기본 볼륨은 임시적이기 때문에  
컨테이너 종료 시 로그가 유실될 위험**이 존재한다.

Crash 로그가 없다면 원인 분석이 어렵고, 특히 **운영 중 발생한 장애에 대한 사후 분석 불가능** 문제로 이어진다.

---

## 3. 고려사항(Considerations)


- **로그 유실 방지**:
  - 컨테이너 외부에 마운트된 경로를 통해 로그/덤프 저장
  - e.g., 네임드 볼륨, 호스트 디렉터리, 객체 저장소 등

- **파일명 중복 방지**:
  - `-XX:ErrorFile=/var/log/jvm_crash/hs_err_%p_%t.log` 형식으로 설정
  - PID와 타임스탬프 기반으로 로그파일 이름 구성

- **컨테이너 종료 지연 설정**:
  - core dump 또는 로그가 **정상적으로 flush 완료되기 전 컨테이너가 제거되지 않도록** 주의
  - Graceful shutdown 시그널 타이밍 확보 필요

- **보안/운영 고려**:
  - 로그 저장 위치는 접근권한 관리 필요
  - 디스크 용량 관리 및 log rotation 등 운영 전략 고려

---

## 4. 최종 결정(Final Decision)

- JVM 종료 시 로그 유실을 방지하기 위해 다음 Java 옵션을 필수 설정한다:
```bash
-XX:+HeapDumpOnOutOfMemoryError \
-XX:ErrorFile=/var/log/jvm_crash/hs_err_%p_%t.log
```

- `/var/log/jvm_crash`는 외부 스토리지(네임드 볼륨 또는 마운트 경로)로 연결되며,  
    컨테이너 재기동 시에도 해당 디렉터리는 유지된다.
- 로그 파일의 이름은 PID와 타임스탬프를 포함해 **중복 없이 저장**되도록 하며,  
    core dump 설정 시에도 같은 저장소에 기록되도록 구성한다.

---

## 5. 기대효과(Expected Benefits)

- JVM 강제 종료 시 로그 유실 없이 저장 가능
- 장애 원인 분석 시 힌트를 얻을 수 있는 핵심 파일 확보
- 로그 저장 위치 통일로 운영 복잡도 감소
- 후속 자동화나 모니터링 연계의 기반 마련

---

## 6. 계속 고민할 사항(Still Open Issues)

- 최종 로그 저장 위치를 네임드 볼륨, 호스트 디렉터리, 혹은 외부 객체 저장소 중 어떤 것으로 설정할지 결정 필요
- 컨테이너 종료 전 로그 flush 타이밍 보장을 위한 lifecycle 관리 필요
- core dump 파일의 용량 관리 및 삭제 주기 전략 필요
- JVM 옵션 외에 추가 도구 도입 여부 검토

---

# 📚 작성 규칙

- 문장은 간결하고 명확하게.
- 하나의 문장에는 하나의 의미만.
- 실제 생각의 흐름에 가깝게 기술할 것.

---
