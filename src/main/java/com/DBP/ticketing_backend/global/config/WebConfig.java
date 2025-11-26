package com.DBP.ticketing_backend.global.config;

import com.DBP.ticketing_backend.global.interceptor.QueueTokenInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final QueueTokenInterceptor queueTokenInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(queueTokenInterceptor)
            // 1. 좌석 조회 차단 (대기열 통과해야 조회 가능)
            .addPathPatterns("/api/seat/**")
            // 2. 예매 생성 차단 (우회 방지)
            // 단, 내 예매 내역 조회(/my)나 취소, 결제는 허용해야 하므로 정확한 경로 지정 필요
            // (Controller 경로가 /api/booking 이라면 아래와 같이 설정)
            .addPathPatterns("/api/booking");
    }
}
