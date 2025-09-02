package com.rihee.alerting.loggingService.architecture.support;

import com.tngtech.archunit.core.domain.JavaClass;
import java.util.List;
import java.util.Set;

/**
 * 아키텍처 테스트에서 공통적으로 사용하는 어설션/출력 유틸리티 모음입니다.
 *
 * <p>주요 기능:
 * <ul>
 *   <li>{@link #simpleNames(Set)} :
 *       {@link JavaClass} 집합을 단순 클래스명(String) 리스트로 변환</li>
 *   <li>{@link #banner(String, String, String, String)} :
 *       발견된 목록, 기대 목록, 누락 목록을 보기 좋게 정리한
 *       배너 문자열을 생성</li>
 * </ul>
 *
 * <p>이 클래스는 테스트 진단 메시지를 일관된 형식으로 보여주기 위해 만들어졌으며,
 * 인스턴스를 생성하지 못하도록 <b>private 생성자</b>를 가지고 있습니다.
 *
 * <h2>예시</h2>
 * <pre>{@code
 * Set<JavaClass> discovered = ...;
 * Set<JavaClass> expected = ...;
 * Set<JavaClass> missing = ...;
 *
 * String msg = ArchAssertions.banner(
 *     "PortSpec 누락 감지!",
 *     ArchAssertions.simpleNames(discovered).toString(),
 *     ArchAssertions.simpleNames(expected).toString(),
 *     ArchAssertions.simpleNames(missing).toString()
 * );
 * assertTrue(missing.isEmpty(), msg);
 * }</pre>
 *
 * @author 리희
 * @since 1.0
 */
public final class ArchAssertions {

  private ArchAssertions() {}

  /**
   * {@link JavaClass} 집합에서 단순 클래스명만 추출하여 리스트로 반환합니다.
   *
   * <p>테스트 결과 메시지에 {@code FQCN} 대신 간결한 클래스명을 출력하기 위한 유틸입니다.
   *
   * @param cs 클래스 집합
   * @return {@link JavaClass#getSimpleName()} 값으로 구성된 리스트
   */
  public static List<String> simpleNames(Set<JavaClass> cs) {
    return cs.stream().map(JavaClass::getSimpleName).toList();
  }

  /**
   * 발견된 목록, 기대 목록, 누락 목록을 보기 좋게 정리한
   * 배너 문자열을 생성합니다.
   *
   * <p>주로 테스트 실패 메시지에 전달하여 어떤 요소가 누락되었는지
   * 한눈에 확인할 수 있도록 돕습니다.
   *
   * @param title 배너 제목 (예: {@code "PortSpec 누락 감지!"})
   * @param found 발견된 요소 목록 문자열
   * @param expected 기대되는 요소 목록 문자열
   * @param missing 누락된 요소 목록 문자열
   * @return 포맷팅된 다중 라인 문자열
   */
  public static String banner(String title, String found, String expected, String missing) {
    return """
        ------------------------------------------------------------
        [%s]
        ------------------------------------------------------------
        발견된 목록 : %s
        기대 목록   : %s
        누락 목록   : %s
        ------------------------------------------------------------
        """.formatted(title, found, expected, missing);
  }
}
