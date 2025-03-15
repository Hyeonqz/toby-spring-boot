package com.example.hellospring.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.hellospring.domain.Payment;

public abstract class PaymentService {
	// 재사용성 높은 코드
	public Payment prepare(Long orderId, String currency, BigDecimal foreignCurrencyAmount) throws IOException {
		BigDecimal krw = this.getExchangeRate(currency);
		BigDecimal convertedAmount = foreignCurrencyAmount.multiply(krw);
		LocalDateTime validUntil = LocalDateTime.now().plusMinutes(30);

		return new Payment(orderId, currency, foreignCurrencyAmount, krw, convertedAmount, validUntil);
	}

	// 요구사항에 따라 바뀔 코드
	abstract BigDecimal getExchangeRate(String currency) throws IOException;
}
