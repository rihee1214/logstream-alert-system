package com.rihee.alerting.common.interceptor;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 설정 클래스입니다.
 * <p>
 * {@link StructuredLogInterceptor}를 모든 HTTP 요청에 적용되도록 등록합니다.
 * 이 인터셉터는 요청마다 MDC 정보를 세팅하고, 요청 종료 시 MDC를 정리하여
 * 로깅에 필요한 컨텍스트 정보를 관리하는 역할을 수행합니다.
 * </p>
 *
 * @see StructuredLogInterceptor
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final StructuredLogInterceptor structuredLogInterceptor;

    /**
     * {@link StructuredLogInterceptor}를 주입받아 WebMvc 설정에 등록합니다.
     *
     * @param structuredLogInterceptor MDC 세팅 및 해제를 위한 인터셉터
     */
    public WebConfig(StructuredLogInterceptor structuredLogInterceptor){
        this.structuredLogInterceptor = structuredLogInterceptor;
    }

    /**
     * 인터셉터 레지스트리에 {@link StructuredLogInterceptor}를 등록합니다.
     *
     * @param registry 인터셉터 레지스트리
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(structuredLogInterceptor);
    }
}
