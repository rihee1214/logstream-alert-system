package com.rihee.alerting.mockservice.scheduler;

import java.util.Set;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// TODO 전체적으로 Registry에서 등록한 것을 검증해서 실패시, 실패 메시지를 내뱉도록 해야한다.
@RestController
@RequestMapping("/mock/scheduler")
public class MockSchedulerController {

  private final SchedulerManager schedulerManager;

  public MockSchedulerController(SchedulerManager schedulerManager) {
    this.schedulerManager = schedulerManager;
  }

  @PostMapping("/register")
  public ResponseEntity<String> register(@RequestBody ScheduleRequestDto request) {
    schedulerManager.register(request.getUrl(), request.getIntervalMs());
    return ResponseEntity.ok("등록 완료");
  }

  @DeleteMapping("/remove")
  public ResponseEntity<String> remove(@RequestParam String url) {
    schedulerManager.remove(url);
    return ResponseEntity.ok("제거 완료");
  }

  @PostMapping("/fail")
  public ResponseEntity<String> setFailure(@RequestParam String url) {
    schedulerManager.setFailure(url, true);
    return ResponseEntity.ok("실패 설정 완료");
  }

  @DeleteMapping("/fail")
  public ResponseEntity<String> removeFailure(@RequestParam String url) {
    schedulerManager.setFailure(url, false);
    return ResponseEntity.ok("실패 해제 완료");
  }

  @GetMapping("/status")
  public Set<String> getStatus() {
    return schedulerManager.getRegisteredUrls();
  }
}
