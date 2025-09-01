package com.rihee.alerting.loggingService.core.runtime;

import com.rihee.alerting.loggingService.core.pipeline.api.CommitableLogProcessor;
import com.rihee.alerting.loggingService.core.pipeline.api.LogProcessorPort;
import com.rihee.alerting.loggingService.core.pipeline.context.DefaultLogProcessingContext;
import com.rihee.alerting.loggingService.core.pipeline.context.LogProcessingContext;
import com.rihee.alerting.loggingService.core.pipeline.result.ProcessResult;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@code LogWorker}는 로그 처리 파이프라인을 실행하는 런타임 작업 단위입니다.
 *
 * <p>이 클래스는 {@link Runnable}을 구현하여 스레드 풀 혹은 독립 스레드에서
 * 실행할 수 있으며, 내부적으로 {@link LogProcessorPort} 체인을 순차적으로 호출하여
 * 로그를 수집 → 검증 → 영속화 등의 단계를 수행합니다.
 *
 * <p>동작 개요:
 * <ol>
 *   <li>{@link LoggingRuntimeConfig}로부터 프로세서 체인을 생성</li>
 *   <li>{@link #run()} 메서드에서 무한 루프를 돌며 {@link #process()} 호출</li>
 *   <li>{@link #process()} 내부에서 각 {@link LogProcessorPort}를 순차 실행</li>
 *   <li>{@link ProcessResult}에 따라 다음 단계 진행 여부 및 커밋 허용 여부 결정</li>
 *   <li>모든 단계가 성공적으로 완료되면 {@link CommitableLogProcessor#commit()} 호출</li>
 * </ol>
 *
 * <p>에러 처리:
 * <ul>
 *   <li>단일 프로세서 실행 중 예외 발생 시: {@code log.warn}으로 경고 기록 후 루프 계속</li>
 *   <li>루프 자체의 치명적 예외 발생 시: {@code log.error}로 기록 후 종료</li>
 *   <li>커밋 중 예외 발생 시: 실패한 프로세서 이름과 함께 경고 로그 출력</li>
 * </ul>
 *
 * <p><b>주의:</b><br>
 * 현재 구현은 무한 루프를 돌도록 설계되어 있어,
 * 애플리케이션 종료 시점에는 외부적으로 스레드 인터럽트나 종료 신호를 전달해야 합니다.
 *
 * @see LogProcessorPort
 * @see CommitableLogProcessor
 * @see LogProcessingContext
 * @see ProcessResult
 */
public class LogWorker implements Runnable {

  private static final Logger log = LoggerFactory.getLogger(LogWorker.class);
  private final List<? extends LogProcessorPort> logProcessors;
  private final List<? extends CommitableLogProcessor> commitableLogProcessors;

  /**
   * 주어진 {@link LoggingRuntimeConfig}를 기반으로
   * 로그 처리 파이프라인을 초기화합니다.
   *
   * <p>생성자 내부에서는:
   * <ul>
   *   <li>{@link LoggingRuntimeConfig#createProcessorChain()}을 호출하여 전체 프로세서 체인 생성</li>
   *   <li>해당 체인에서 {@link CommitableLogProcessor}만 별도로 추출하여 커밋 대상 목록 생성</li>
   * </ul>
   *
   * @param config 파이프라인 구성을 정의한 런타임 설정 객체
   */
  public LogWorker(LoggingRuntimeConfig config) {
    this.logProcessors = config.createProcessorChain();
    this.commitableLogProcessors = logProcessors.stream()
                                            .filter(CommitableLogProcessor.class::isInstance)
                                            .map(CommitableLogProcessor.class::cast)
                                            .collect(Collectors.toList());
  }

  /**
   * 로그 파이프라인을 무한 루프 형태로 실행합니다.
   *
   * <p>실행 중 예외가 발생하면 경고 로그를 남기고 다음 반복을 계속 수행합니다.
   * 루프 자체에서 치명적인 예외가 발생할 경우에는 에러 로그를 남기고 종료됩니다.
   */
  @Override
  public void run() {
    // TODO 동작 처리 및 로그 처리 관련 고민 필요
    log.info("LogWorker started. Processor chain size: {}", logProcessors.size());
    try {
      while (!Thread.currentThread().isInterrupted()) {
        try {
          process();
        } catch (RuntimeException e) {
          // 단일 사이클에서 발생한 예외 → 경고 로그로 남기고 루프는 계속
          // TODO 어떤 프로세스에서 문제가 발생했는지 알리는 요소가 필요할 수 있음
          log.warn("로그 처리 사이클에서 예외 발생. 다음 사이클을 계속 진행합니다.", e);
        }
      }
    } catch (Exception e) {
      // 루프 전체를 죽이는 예외 → 치명적 로그
      log.error("LogWorker 실행 중 치명적 예외 발생. 워커를 종료합니다.", e);
    } finally {
      log.info("LogWorker stopped.");
    }
  }

  /**
   * 단일 로그 처리 사이클을 수행합니다.
   *
   * <p>처리 흐름:
   * <ol>
   *   <li>새 {@link DefaultLogProcessingContext}를 생성</li>
   *   <li>체인에 등록된 모든 {@link LogProcessorPort}를 순차적으로 실행</li>
   *   <li>{@link ProcessResult#shouldContinue()}가 {@code false}인 경우 체인 실행 중단</li>
   *   <li>{@link ProcessResult#shouldCommit()} 결과에 따라 커밋 여부 결정</li>
   *   <li>커밋이 허용되면 {@link CommitableLogProcessor#commit()}을 각 대상에 호출</li>
   * </ol>
   *
   * <p>예외 발생 시:
   * <ul>
   *   <li>프로세서 처리 중 예외 → 경고 로그만 남기고 루프는 지속</li>
   *   <li>커밋 중 예외 → 해당 프로세서 이름과 함께 경고 로그 출력</li>
   * </ul>
   */
  private void process() {
    LogProcessingContext context = new DefaultLogProcessingContext();

    boolean commitPermitted = true;

    for (LogProcessorPort processor : logProcessors) {
      ProcessResult result = processor.process(context);
      if (!result.shouldContinue()) {
        commitPermitted = result.shouldCommit();
        break;
      }
      context = result.context();
    }

    if (commitPermitted) {
      for (CommitableLogProcessor commitTarget : commitableLogProcessors) {
        try {
          commitTarget.commit();
        } catch (Exception e) {
          log.warn("커밋 실패: {}", commitTarget.getClass().getSimpleName(), e);
        }
      }
    }
  }
}
