package com.rihee.alerting.loggingService.tools.constants;

import com.rihee.alerting.loggingService.annotations.CollectorType;
import com.rihee.alerting.loggingService.annotations.PersistenceType;
import com.rihee.alerting.loggingService.annotations.ValidatorType;
import java.lang.annotation.Annotation;

/**
 * 로그 처리 과정에서 사용되는 애노테이션 타입과,
 * 해당 애노테이션을 기준으로 생성되는 레지스트리 JSON 리소스 경로를 매핑하는 열거형(enum) 클래스입니다.
 *
 * <p>이 열거형은 런타임에서 특정 애노테이션 타입을 기준으로
 * 레지스트리 파일을 찾을 수 있도록 도와줍니다.
 * 레지스트리 파일은 {@code META-INF/logging/} 경로 아래에 위치하며,
 * 키(예: "kafka")와 해당 키에 매핑된 구현 클래스의 FQCN(fully qualified class name)을 포함합니다.</p>
 *
 * <p>각 상수는 로그 파이프라인을 구성하는 세 가지 주요 컴포넌트 종류를 나타냅니다:</p>
 * <ul>
 *   <li>{@link #COLLECTOR} – {@link CollectorType} 애노테이션이 붙은 클래스.
 *       외부로부터 로그를 수집하는 역할을 합니다.</li>
 *   <li>{@link #VALIDATOR} – {@link ValidatorType} 애노테이션이 붙은 클래스.
 *       로그 메시지를 유효성 검증하는 역할을 합니다.</li>
 *   <li>{@link #PERSISTENCE} – {@link PersistenceType} 애노테이션이 붙은 클래스.
 *       로그 메시지를 데이터베이스와 같은 저장소에 영속화하는 역할을 합니다.</li>
 * </ul>
 *
 * <p>애노테이션 프로세서는 각 상수에 대응하는 JSON 파일을
 * {@code META-INF/logging/} 디렉토리에 생성합니다.
 * 이 열거형은 해당 애노테이션 타입과 파일 경로를 함께 보관합니다.</p>
 */
public enum ProcessorRegistryPaths {

  /**
   * {@link CollectorType} 애노테이션이 붙은 클래스의 레지스트리 정보.
   *
   * <p>대응되는 JSON 파일 경로:
   * {@code META-INF/logging/CollectorType.json}</p>
   */
  COLLECTOR(CollectorType.class, "META-INF/logging/CollectorType.json"),
  /**
   * {@link ValidatorType} 애노테이션이 붙은 클래스의 레지스트리 정보.
   *
   * <p>대응되는 JSON 파일 경로:
   * {@code META-INF/logging/ValidatorType.json}</p>
   */
  VALIDATOR(ValidatorType.class, "META-INF/logging/ValidatorType.json"),
  /**
   * {@link PersistenceType} 애노테이션이 붙은 클래스의 레지스트리 정보.
   *
   * <p>대응되는 JSON 파일 경로:
   * {@code META-INF/logging/PersistenceType.json}</p>
   */
  PERSISTENCE(PersistenceType.class, "META-INF/logging/PersistenceType.json");

  private final Class<? extends Annotation> type;
  private final String filePath;

  ProcessorRegistryPaths(Class<? extends Annotation> type, String filePath) {
    this.type = type;
    this.filePath = filePath;
  }

  /**
   * 주어진 애노테이션 타입과 일치하는 {@link ProcessorRegistryPaths} 상수를 반환합니다.
   *
   * <p>열거형에 등록된 애노테이션 타입과 비교하여,
   * 해당 타입이 존재하면 그에 해당하는 enum 상수를 반환합니다.
   * 존재하지 않을 경우 {@link IllegalArgumentException} 예외가 발생합니다.</p>
   *
   * @param type 확인할 애노테이션 타입
   * @return 대응되는 {@link ProcessorRegistryPaths} 상수
   * @throws IllegalArgumentException 주어진 타입과 일치하는 파일 경로가 없을 경우 발생
   */
  public static ProcessorRegistryPaths fromType(Class<? extends Annotation> type) {
    for (ProcessorRegistryPaths path : ProcessorRegistryPaths.values()) {
      if (path.getType().isAssignableFrom(type)) {
        return path;
      }
    }
    throw new IllegalArgumentException("파라미터로 넣은 Type과 맞는 filePath가 존재하지 않습니다: " + type);
  }

  /**
   * 이 상수와 연결된 애노테이션 타입을 반환합니다.
   *
   * @return 애노테이션 타입 (예: {@link CollectorType}, {@link ValidatorType}, {@link PersistenceType})
   */
  public Class<? extends Annotation> getType() {
    return this.type;
  }

  /**
   * 이 상수와 연결된 레지스트리 JSON 파일 경로를 반환합니다.
   *
   * @return {@code META-INF/logging/} 아래에 위치한 JSON 파일 경로
   */
  public String getFilePath() {
    return this.filePath;
  }
}
