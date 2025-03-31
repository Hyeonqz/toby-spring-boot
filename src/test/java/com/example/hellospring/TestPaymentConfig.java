package com.example.hellospring;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.hellospring.exrate.impl.CachedExRateProvider;
import com.example.hellospring.payment.ExRateProvider;
import com.example.hellospring.payment.ExRateProviderStub;
import com.example.hellospring.payment.PaymentService;

@Configuration
public class TestPaymentConfig {

	@Bean
	public PaymentService paymentService () {
		return new PaymentService(cachedExRateProvider(), clock());
	}

	@Bean
	public ExRateProvider cachedExRateProvider() {
		return new CachedExRateProvider(exRateProvider());
	}

	@Bean
	public ExRateProvider exRateProvider () {
		return new ExRateProviderStub(BigDecimal.valueOf(1_000));
	}

	@Bean
	public Clock clock() {
		return Clock.fixed(Instant.now(), ZoneId.systemDefault());
	}

}
