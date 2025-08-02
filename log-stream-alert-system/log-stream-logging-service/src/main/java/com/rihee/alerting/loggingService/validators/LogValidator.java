package com.rihee.alerting.loggingService.validators;

import com.rihee.alerting.loggingService.core.pipeline.LogProcessor;
import java.util.Map;

public abstract class LogValidator implements LogProcessor {

  public interface Builder<T extends LogValidator> {

    /**
     * 설정 정보를 바탕으로 빌더 내부 상태를 구성합니다.
     *
     * @param setting key-value 형태의 설정 정보
     * @return 현재 빌더 인스턴스 (메서드 체이닝 가능)
     */
    Builder<T> withProperties(Map<String, String> setting);

    /**
     * 설정에 기반하여 검증기 인스턴스를 생성합니다.
     *
     * @return {@code LogValidator} 구현체 인스턴스
     */
    T build();
  }
}
