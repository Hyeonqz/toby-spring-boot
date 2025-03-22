package com.example.hellospring.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObjectFactory {

	// Java 코드로 된 구성정보
	// 빈 클래스, 의존관계 정의
	@Bean
	public PaymentService paymentService () {
		return new PaymentService(cachedExRateProvider());
	}

	@Bean
	public ExRateProvider cachedExRateProvider() {
		return new CachedExRateProvider(exRateProvider());
	}

	@Bean
	public ExRateProvider exRateProvider () {
		return new WebApiExRateProvider();
	}

}
