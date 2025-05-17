package com.rihee.alerting.common;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 테스트 환경에서 공통 모듈(Spring Bean, Configuration 등)을 로딩하기 위한
 * 테스트 전용 Spring Boot Application 클래스입니다.
 *
 * <p>이 클래스는 테스트 context 초기화를 위한 용도이며,
 * 테스트 대상 클래스가 아닙니다.
 */
@SpringBootApplication(scanBasePackages = "com.rihee.alerting.common")
public class CommonTestBootstrap {
}
