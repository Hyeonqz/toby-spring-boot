package com.example.hellospring;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import com.example.hellospring.api.ApiTemplate;
import com.example.hellospring.api.ErApiExRateExtractor;
import com.example.hellospring.api.SimpleApiExecutor;
import com.example.hellospring.exrate.impl.CachedExRateProvider;
import com.example.hellospring.exrate.impl.WebApiExRateProvider;
import com.example.hellospring.payment.ExRateProvider;
import com.example.hellospring.payment.PaymentService;

@Configuration
public class PaymentConfig {

	// Java 코드로 된 구성정보
	// 빈 클래스, 의존관계 정의
	@Bean
	public PaymentService paymentService () {
		return new PaymentService(exRateProvider(), clock());
	}

	@Bean
	public RestTemplate restTemplate () {
		return new RestTemplate();
	}

	@Bean
	public ExRateProvider cachedExRateProvider() {
		return new CachedExRateProvider(exRateProvider());
	}

	@Bean
	public ApiTemplate apiTemplate() {
		return new ApiTemplate(new SimpleApiExecutor(), new ErApiExRateExtractor());
	}

	@Bean
	public ExRateProvider exRateProvider () {
		return new WebApiExRateProvider(this.apiTemplate());
	}

	@Bean
	public Clock clock() {
		return Clock.systemDefaultZone();
	}

}
