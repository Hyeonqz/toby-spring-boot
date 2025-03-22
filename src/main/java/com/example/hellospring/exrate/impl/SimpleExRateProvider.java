package com.example.hellospring.exrate.impl;

import java.math.BigDecimal;

import com.example.hellospring.payment.ExRateProvider;

public class SimpleExRateProvider implements ExRateProvider {
	private static final String USD = "USD";

	@Override
	public BigDecimal getExRate (String currency) {
		if(currency.equals(USD))
			return BigDecimal.valueOf(1000);

		throw new IllegalArgumentException("지원되지 않는 통화 입니다.");
	}

}
