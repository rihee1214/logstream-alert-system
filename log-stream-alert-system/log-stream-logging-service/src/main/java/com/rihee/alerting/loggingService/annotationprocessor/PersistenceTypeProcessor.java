package com.rihee.alerting.loggingService.annotationprocessor;

import com.rihee.alerting.loggingService.annotations.PersistenceType;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

/**
 * {@code PersistenceTypeProcessor}는
 * {@link PersistenceType}어노테이션이 부여된 클래스에 대해
 * 컴파일 타임 검증을 수행하는 annotation processor입니다.
 *
 * <p>다음과 같은 규칙을 강제합니다:
 * <ul>
 *   <li><b>중복 금지:</b> 동일한 {@code @PersistenceType.value()} 값을 가진 클래스가 둘 이상 존재하면
 *                  컴파일 오류를 발생시킵니다.</li>
 *   <li><b>정적 팩토리 메서드 요구:</b> 어노테이션이 부여된 클래스는 반드시 {@code public static builder()} 메서드를 정의해야 하며,
 *       이는 외부에서 인스턴스를 생성하는 진입점으로 사용됩니다.
 *   </li>
 * </ul>
 *
 * <p>이 프로세서는 {@code META-INF/services/javax.annotation.processing.Processor} 파일을 통해
 * 서비스 로딩되어 Java 컴파일러에 의해 자동으로 실행됩니다.
 *
 * <p>이 검사는 런타임 오류를 방지하고, 설정 기반 로그 수집기 로딩 시스템이 안정적으로 동작하기 위한 사전 조건을 보장합니다.
 *
 * <p><b>주의:</b> 이 processor는 반드시 {@code @SupportedAnnotationTypes}에
 * {@code "com.rihee.alerting.loggingService.annotations.PersistenceType"}을 명시해야 하며,
 * Gradle이나 Maven 빌드 시스템에서는 반드시 processor path와 resources 등록이 필요합니다.
 *
 * @see PersistenceType
 * @see AbstractProcessor
 */
@SupportedAnnotationTypes("com.rihee.alerting.loggingService.annotations.PersistenceType")
public class PersistenceTypeProcessor extends AbstractProcessor {

  private final Map<String, String> persistenceTypes = new HashMap<>();

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    for (Element element : roundEnv.getElementsAnnotatedWith(PersistenceType.class)) {
      if (element.getKind() != ElementKind.CLASS) {
        continue;
      }

      PersistenceType annotation = element.getAnnotation(PersistenceType.class);
      String key = annotation.value();
      String className = ((TypeElement) element).getQualifiedName().toString();

      // CollectorType annotation의 value 변수의 중복 방지 검사
      if (persistenceTypes.containsKey(key)) {
        processingEnv.getMessager().printMessage(
            Diagnostic.Kind.ERROR,
            "중복된 @PersistenceType 값: " + key
                + " (" + className + " / " + persistenceTypes.get(key) + ")",
            element
        );
      } else {
        persistenceTypes.put(key, className);
      }

      // static builder() 존재 여부 검사
      boolean hasBuilder = ((TypeElement) element).getEnclosedElements()
          .stream()
          .filter(e -> e.getKind() == ElementKind.METHOD)
          .map(e -> (ExecutableElement) e)
          .anyMatch(method -> method.getSimpleName().toString().equals("builder")
              && method.getModifiers().contains(Modifier.STATIC));

      if (!hasBuilder) {
        processingEnv.getMessager().printMessage(
            Diagnostic.Kind.ERROR,
            className + " 클래스에는 static builder() 메서드가 필요합니다.",
            element
        );
      }
    }
    return true;
  }
}
