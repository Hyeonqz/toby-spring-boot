package com.example.hellospring.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.hellospring.domain.Payment;

public class PaymentService {
	// 한번만 만들어두고 재사용을 한다.
	private final ExRateProvider exRateProvider;

	// 의존관계 설정 책임 -> 위 책임을 PaymentService 가 가지고 있음.
	public PaymentService (ExRateProvider exRateProvider) {
		this.exRateProvider = exRateProvider;
	}

	// 재사용성 높은 코드
	public Payment prepare(Long orderId, String currency, BigDecimal foreignCurrencyAmount) throws IOException {
		BigDecimal exRate = exRateProvider.getExRate(currency);
		BigDecimal convertedAmount = foreignCurrencyAmount.multiply(exRate);
		LocalDateTime validUntil = LocalDateTime.now().plusMinutes(30);

		return new Payment(orderId, currency, foreignCurrencyAmount, exRate, convertedAmount, validUntil);
	}

}
