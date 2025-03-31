package com.example.hellospring.payment;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

@Component
public class PaymentService {
	// 한번만 만들어두고 재사용을 한다.
	private final ExRateProvider exRateProvider;
	private final Clock clock;

	// 의존관계 설정 책임 -> 위 책임을 PaymentService 가 가지고 있음.
	public PaymentService (ExRateProvider exRateProvider, Clock clock) {
		this.exRateProvider = exRateProvider;
		this.clock = clock;
	}

	// 재사용성 높은 코드
	public Payment prepare(Long orderId, String currency, BigDecimal foreignCurrencyAmount) throws IOException {
		BigDecimal exRate = exRateProvider.getExRate(currency);


		return Payment.createPrepare(orderId, currency, foreignCurrencyAmount, exRate, LocalDateTime.now());
	}

}
