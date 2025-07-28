package com.rihee.alerting.loggingService.annotationprocessor;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

/**
 * {@code AbstractTypeProcessor}는 특정 커스텀 애너테이션(@interface)을 기반으로,
 * 애너테이션이 부여된 클래스들에 대한 컴파일 타임 검증 로직을 구성하기 위한 공통 베이스 클래스입니다.
 *
 * <p>이 클래스를 상속받는 서브 클래스는 반드시 {@link #getTargetAnnotationType()} 메서드를 구현하여
 * 자신이 검사하고자 하는 애너테이션 타입을 명시해야 합니다.
 * 예: {@code ValidatorType.class}, {@code CollectorType.class}, {@code PersistenceType.class} 등
 *
 * <p>기본 제공되는 검증 로직은 다음과 같습니다:
 * <ul>
 *   <li>{@link NamedTypeConflictProcessor}:
 *     애너테이션의 {@code value()} 속성이 중복되었는지 검사합니다. 동일한 식별자 값이 여러 클래스에 존재하면 컴파일 오류를 발생시킵니다.</li>
 *   <li>{@link BuilderCheckProcessorLogic}:
 *     애너테이션이 붙은 클래스에 {@code public static builder()} 메서드가 존재하는지 검사합니다. 없을 경우 컴파일 오류를 발생시킵니다.</li>
 * </ul>
 *
 * <p>이 외의 사용자 정의 검증 로직이 필요한 경우,
 * {@link ProcessorLogic} 인터페이스를 구현한 클래스를 {@link #processorLogics} 리스트에 추가하면 됩니다.
 *
 * <p>이 프로세서는 {@link AbstractProcessor}를 상속받아 Java 컴파일러(javac)에 의해 자동 실행됩니다.
 * 컴파일 타임에 강제 규칙을 부여함으로써 런타임 오류를 사전에 방지할 수 있습니다.
 *
 * <h3>사용 예시 (서브 클래스)</h3>
 * <pre>{@code
 * @SupportedAnnotationTypes("com.example.annotations.ValidatorType")
 * public class ValidatorTypeProcessor extends AbstractTypeProcessor {
 *     @Override
 *     protected Class<? extends Annotation> getTargetAnnotationType() {
 *         return ValidatorType.class;
 *     }
 * }
 * }</pre>
 *
 * @see javax.annotation.processing.AbstractProcessor
 * @see ProcessorLogic
 */
public abstract class AbstractTypeProcessor extends AbstractProcessor {

  /** 검증 대상 로직들을 구성하는 리스트. 각 {@link ProcessorLogic}은 독립적으로 동작한다. */
  protected final List<ProcessorLogic> processorLogics;

  /**
   * 기본 생성자.
   *
   * <p>이 생성자는 상속 클래스에서 호출되어야 하며, 기본적으로 공통 ProcessorLogic들을 등록합니다.
   * 서브클래스에서 추가 로직이 필요한 경우, {@link #addValidationLogic(ProcessorLogic)} 메서드를 이용해야 하며,
   * 생성자를 오버라이드하거나 {@code processorLogics}를 직접 수정하지 않아야 합니다.
   * </p>
   *
   * <p>해당 생성자는 JDK의 {@code Processor} SPI 로딩 메커니즘에 의해 자동 호출되므로,
   * 파라미터를 가지지 않아야 하며, 내부에서 필요한 모든 초기화를 수행해야 합니다.
   *
   * <p>{@link NamedTypeConflictProcessor}와 {@link BuilderCheckProcessorLogic}를 기본 검증 로직으로 포함한다.
   */
  protected AbstractTypeProcessor() {
    processorLogics = new ArrayList<>();
    processorLogics.add(new NamedTypeConflictProcessor());
    processorLogics.add(new BuilderCheckProcessorLogic());
  }

  /**
   * 이 추상 메서드는 해당 프로세서가 검사할 대상 애너테이션 타입을 반환해야 합니다.
   *
   * <p>반환되는 클래스는 반드시 {@link java.lang.annotation.Annotation}의 서브 타입이어야 하며,
   * 예를 들어 다음과 같은 커스텀 어노테이션 클래스가 올 수 있습니다:
   * <ul>
   *   <li>{@code ValidatorType.class}</li>
   *   <li>{@code CollectorType.class}</li>
   *   <li>{@code PersistenceType.class}</li>
   * </ul>
   *
   * <p>이 메서드는 내부적으로 모든 검증 로직에서 공통적으로 사용되므로,
   * 반환 값이 null이거나 부적절할 경우 모든 검증이 실패할 수 있습니다.
   *
   * @return 검사 대상 애너테이션 타입 클래스 객체
   */
  protected abstract Class<? extends Annotation> getTargetAnnotationType();

  /**
   * 검증 로직을 동적으로 추가할 수 있도록 지원하는 확장 메서드입니다.
   *
   * <p>이 메서드는 서브클래스가 자신만의 {@link ProcessorLogic} 구현체를
   * {@link #processorLogics}에 추가할 수 있는 유일한 수단입니다.
   *
   * <p>기본적으로 {@code processorLogics} 필드는 {@code protected}로 선언되어 있지만,
   * 외부에서 직접 수정하지 않고 본 메서드를 통해 추가하는 것을 권장합니다.
   *
   * @implSpec
   *     이 메서드는 {@code null} 입력을 무시하며, 로직의 중복 삽입 여부나 실행 순서는 보장하지 않습니다.<br>
   *     필요한 경우 서브클래스에서 중복 여부 확인이나 정렬 전략을 함께 관리해야 합니다.
   *
   *
   * @param logic 추가할 {@link ProcessorLogic} 구현체
   */
  protected void addValidationLogic(ProcessorLogic logic) {
    if (logic != null) {
      this.processorLogics.add(logic);
    }
  }

  /**
   * Java 컴파일러(javac)가 어노테이션 처리 과정에서 호출하는 진입점 메서드입니다.
   *
   * <p>이 메서드는 컴파일러 라운드마다 실행되며, {@code processorLogics}에 등록된 검증 로직들을 순차적으로 실행합니다.
   * 각 로직은 {@link RoundEnvironment}를 기반으로 해당 어노테이션이 붙은 엘리먼트들을 스캔하고,
   * 규칙 위반이 감지되면 컴파일 오류를 발생시킵니다.
   *
   * <p>본 구현은 어노테이션 처리 완료를 의미하기 위해 항상 {@code true}를 반환합니다.
   *
   * @param annotations 현재 라운드에서 처리되는 어노테이션 집합 (사용하지 않음)
   * @param roundEnv 컴파일 환경 내 어노테이션이 적용된 엘리먼트 정보
   * @return {@code true} - 추가 처리 불필요함을 나타냅니다.
   */
  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    for (ProcessorLogic logic : this.processorLogics) {
      logic.process(roundEnv);
    }
    return true;
  }

  /**
   * 주어진 {@code Element}에 부여된 애너테이션 중 지정된 {@code targetType}에 대해,
   * 해당 애너테이션의 {@code attributeName} 속성 값을 문자열로 추출합니다.
   *
   * <p>이 메서드는 리플렉션을 사용하지 않고, 컴파일 타임 모델인 {@link AnnotationMirror}를 통해
   * 어노테이션의 메타 정보를 안전하게 조회할 수 있도록 설계되어 있습니다.
   *
   * @param element         애너테이션이 부여된 대상 엘리먼트
   * @param targetType      추출할 애너테이션 타입 (예: {@code CollectorType.class})
   * @param attributeName   추출할 애너테이션 속성 이름 (예: {@code "value"})
   * @return 해당 속성의 문자열 값, 존재하지 않을 경우 {@code null}
   *
   * @implNote {@link AnnotationMirror} 기반 처리로 인해, 런타임 리플렉션이 아닌 컴파일 타임 추론에 적합합니다.
   */
  @SuppressWarnings({"SameParameterValue"})
  protected final String extractAnnotationValue(Element element,
                                                          Class<? extends Annotation> targetType,
                                                          String attributeName) {
    for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
      if (!mirror.getAnnotationType().toString().equals(targetType.getName())) {
        continue;
      }

      for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry :
          mirror.getElementValues().entrySet()) {
        if (entry.getKey().getSimpleName().toString().equals(attributeName)) {
          return entry.getValue().getValue().toString();
        }
      }
    }
    return null;
  }

  /**
   * {@code ProcessorLogic}은 {@link AbstractTypeProcessor}에서 실행 가능한
   * 개별 애너테이션 처리 로직을 정의하기 위한 전략 인터페이스입니다.
   *
   * <p>이 인터페이스를 구현함으로써 애너테이션 처리 시점에 필요한 커스텀 유효성 검사를 구성할 수 있으며,
   * {@link AbstractTypeProcessor#processorLogics}에 등록되어 순차적으로 실행됩니다.
   *
   * <h3>구현 시 유의사항</h3>
   * <ul>
   *   <li>이 인터페이스의 구현체는 반드시 <strong>stateless</strong> 해야 합니다.
   *       Annotation Processing은 컴파일 중 여러 라운드에 걸쳐 반복 실행될 수 있으며,
   *       상태를 클래스 필드로 유지할 경우 <strong>의도치 않은 캐시, 누락, 누수 현상</strong>이 발생할 수 있습니다.
   *   </li>
   *   <li>상태가 필요한 경우에는 반드시 {@code process()} 메서드 내의 <strong>지역 변수</strong>로 관리해야 하며,
   *       처리 로직은 순수 함수적 패턴을 따르는 것이 좋습니다.
   *   </li>
   *   <li>각 {@code process()} 호출은 독립적이어야 하며,
   *       이전 라운드의 실행 결과에 의존하지 않아야 합니다.
   *   </li>
   * </ul>
   *
   * <p>구현 예시:
   * <pre>{@code
   *   class CheckNonEmptyProcessor implements ProcessorLogic {
   *     @Override
   *     public void process(RoundEnvironment roundEnv) {
   *       // 특정 어노테이션이 부여된 클래스가 필드를 반드시 하나 이상 가져야 한다는 검사
   *     }
   *   }
   * }</pre>
   *
   * @see AbstractTypeProcessor
   * @see RoundEnvironment
   */
  protected abstract static class ProcessorLogic {

    protected abstract void process(RoundEnvironment roundEnv);

    /**
     * 두 ProcessorLogic 인스턴스가 같은 클래스 타입이면 동일한 객체로 간주합니다.
     *
     * <p>이 구현은 상태를 가지지 않는 로직 클래스에 적합하며, 동일한 역할을 수행하는
     * 인스턴스가 여러 개 존재하더라도 중복되지 않도록 하기 위한 용도로 사용됩니다.
     *
     * <p>예를 들어 {@link java.util.Set}에서 중복 방지를 하거나,
     * 특정 로직의 중복 삽입 여부를 확인할 때 유용합니다.
     *
     * @param obj 비교할 객체
     * @return 동일한 클래스의 인스턴스라면 {@code true}, 그렇지 않으면 {@code false}
     */
    @Override
    public boolean equals(Object obj) {
      return obj != null && this.getClass() == obj.getClass();
    }

    /**
     * 클래스 기반 해시코드 반환.
     *
     * <p>같은 클래스 타입의 모든 인스턴스가 동일한 해시코드를 가지므로,
     * 상태가 없는 로직 객체들의 비교 및 중복 관리에 적합합니다.
     *
     * @return 클래스 기반 해시코드
     */
    @Override
    public int hashCode() {
      return this.getClass().hashCode();
    }
  }

  /**
   * {@code value()} 속성 값의 중복을 검사하는 ProcessorLogic 구현체입니다.
   *
   * <p>동일한 식별자 값이 두 개 이상 존재하면 컴파일 오류를 발생시킵니다.
   */
  protected final class NamedTypeConflictProcessor extends ProcessorLogic {

    @Override
    public void process(RoundEnvironment roundEnv) {
      Map<String, String> foundKeys = new HashMap<>();
      Class<? extends Annotation> annotationType = getTargetAnnotationType();

      for (Element element : roundEnv.getElementsAnnotatedWith(annotationType)) {
        if (element.getKind() != ElementKind.CLASS) {
          continue;
        }

        String value = extractAnnotationValue(element, annotationType, "value");
        String className = ((TypeElement) element).getQualifiedName().toString();

        if (value == null) {
          processingEnv.getMessager().printMessage(
              Diagnostic.Kind.ERROR,
              String.format("@%s 에서 value() 값을 추출할 수 없습니다.", annotationType.getSimpleName()),
              element
          );
          continue;
        }

        if (foundKeys.containsKey(value)) {
          processingEnv.getMessager().printMessage(
              Diagnostic.Kind.ERROR,
              String.format("중복된 @%s 값: %s (%s / %s)",
                  annotationType.getSimpleName(), value,
                  className, foundKeys.get(value)),
              element
          );
        } else {
          foundKeys.put(value, className);
        }
      }
    }
  }

  /**
   * 대상 클래스에 {@code public static builder()} 메서드가 존재하는지 검사하는 ProcessorLogic 구현체입니다.
   *
   * <p>존재하지 않을 경우 컴파일 오류를 발생시킵니다.
   */
  protected final class BuilderCheckProcessorLogic extends ProcessorLogic {

    @Override
    public void process(RoundEnvironment roundEnv) {
      Class<? extends Annotation> targetAnnotationType = getTargetAnnotationType();

      for (Element element : roundEnv.getElementsAnnotatedWith(targetAnnotationType)) {
        if (element.getKind() != ElementKind.CLASS) {
          continue;
        }
        boolean hasBuilder = ((TypeElement) element).getEnclosedElements()
            .stream()
            .filter(e -> e.getKind() == ElementKind.METHOD)
            .map(e -> (ExecutableElement) e)
            .anyMatch(method -> method.getSimpleName().toString().equals("builder")
                && method.getModifiers().contains(Modifier.STATIC));

        if (!hasBuilder) {
          String className = ((TypeElement) element).getQualifiedName().toString();
          processingEnv.getMessager().printMessage(
              Diagnostic.Kind.ERROR,
              className + " 클래스에는 static builder() 메서드가 필요합니다.",
              element
          );
        }
      }
    }
  }
}
