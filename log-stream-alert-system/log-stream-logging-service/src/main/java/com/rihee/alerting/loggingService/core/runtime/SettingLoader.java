package com.rihee.alerting.loggingService.core.runtime;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * {@code SettingLoader}는 로그 수집 시스템의 설정 정보를 외부로부터 로드하여,
 * {@link LoggingRuntimeConfig} 객체로 변환하는 역할을 담당하는 설정 로딩 유틸리티 클래스입니다.
 *
 * <p>이 클래스는 <b>설정 로딩의 단일 진입점(Single Point of Entry)</b>으로 사용되며,
 * 설정 파일의 위치나 형식과 무관하게 <b>다양한 입력 소스</b>로부터 설정을 주입할 수 있는 구조를 지향합니다.
 *
 * <p>현재는 classpath 상의 {@code config/logging.properties} 파일을 기준으로 설정을 로딩하지만,
 * 향후 다음과 같은 입력 소스를 지원하는 메서드로 확장될 수 있습니다:
 * <ul>
 *   <li>외부 경로 (e.g., 파일 시스템의 절대/상대 경로)</li>
 *   <li>네트워크 URL</li>
 *   <li>Spring 환경에서 주입된 {@link java.util.Properties}</li>
 *   <li>환경 변수 기반 설정</li>
 * </ul>
 *
 * <h3>설계 원칙</h3>
 * <ul>
 *   <li>설정 소스의 다양성을 내부적으로 추상화하여 외부에서 호출 방식만 선택 가능하도록 구성</li>
 *   <li>애플리케이션 기동 시점에 유효성을 명확하게 검증</li>
 *   <li>설정이 실패할 경우 <strong>즉시 실패(Fail Fast)</strong> 전략 적용</li>
 * </ul>
 *
 * <p>모든 메서드는 설정을 {@link LoggingRuntimeConfig} 객체로 변환하여 반환합니다.
 *
 * @see LoggingRuntimeConfig
 * @see java.util.Properties
 */
public class SettingLoader {

  /**
   * classpath의 {@code config/logging.properties} 파일을 로드하여
   * {@link LoggingRuntimeConfig} 객체로 변환합니다.
   *
   * <p>해당 메서드는 클래스패스 기준으로 설정 파일을 찾으며, 설정 파일이 존재하지 않거나 읽기에 실패할 경우
   * 예외를 발생시켜 설정 오류를 명확히 드러냅니다.
   *
   * <h3>기본 기대 위치</h3>
   * <pre>
   * src/main/resources/config/logging.properties
   * </pre>
   *
   * <h3>예외 처리</h3>
   * <ul>
   *   <li>설정 파일이 없을 경우: {@link FileNotFoundException} → {@link IllegalStateException}으로 래핑</li>
   *   <li>입출력 오류 발생 시: {@link IOException} → {@link IllegalStateException}으로 래핑</li>
   * </ul>
   *
   * @return {@link LoggingRuntimeConfig} 인스턴스
   * @throws IllegalStateException 설정 파일이 누락되었거나 파싱에 실패한 경우 발생
   */
  public static LoggingRuntimeConfig loadRuntimeSettingFromClasspath() {
    Properties setting = new Properties();

    try (InputStream is = SettingLoader.class.getClassLoader()
                              .getResourceAsStream("config/logging.properties")) {
      if (is == null) {
        throw new FileNotFoundException("설정 파일이 classpath에 존재하지 않습니다: config/logging.properties");
      }
      setting.load(is);
    } catch (IOException e) {
      throw new IllegalStateException(
          "설정 파일을 classpath에서 로드할 수 없습니다: config/logging.properties", e);
    }
    return LoggingRuntimeConfig.from(setting);
  }

}
