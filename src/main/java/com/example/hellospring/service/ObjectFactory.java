package com.example.hellospring.service;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "com.example.hellospring.*")
public class ObjectFactory {
	// 구성정보
	// 빈 클래스, 의존관계 정의
/*	@Bean
	public PaymentService paymentService () {
		return new PaymentService(exRateProvider());
	}

	@Bean
	public ExRateProvider exRateProvider () {
		return new SimpleExRateProvider();
	}*/

}
