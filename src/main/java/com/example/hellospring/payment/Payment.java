package com.example.hellospring.payment;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;

import lombok.Getter;

@Getter
public class Payment {
	private Long orderId;
	private String currency;
	private BigDecimal foreignCurrencyAmount;
	private BigDecimal exRate;
	private BigDecimal convertedAmount;
	private LocalDateTime validUntil;

	public Payment (Long orderId, String currency, BigDecimal foreignCurrencyAmount, BigDecimal exRate,
		BigDecimal convertedAmount, LocalDateTime validUntil) {
		this.orderId = orderId;
		this.currency = currency;
		this.foreignCurrencyAmount = foreignCurrencyAmount;
		this.exRate = exRate;
		this.convertedAmount = convertedAmount;
		this.validUntil = validUntil;
	}

	public Payment () {
	}

	public static Payment createPrepare(Long orderId, String currency, BigDecimal foreignCurrencyAmount, BigDecimal exRate, LocalDateTime now) {

		BigDecimal convertedAmount = foreignCurrencyAmount.multiply(exRate);
		LocalDateTime validUntil = now.plusMinutes(30);

		return new Payment(orderId, currency, foreignCurrencyAmount, exRate, convertedAmount, validUntil);
	}

	public boolean isValid(Clock clock) {
		return LocalDateTime.now(clock).isBefore(this.validUntil);
	}


	@Override
	public String toString () {
		return "Payment{" +
			"orderId=" + orderId +
			", currency='" + currency + '\'' +
			", foreignCurrencyAmount=" + foreignCurrencyAmount +
			", exRate=" + exRate +
			", convertedAmount=" + convertedAmount +
			", validUntil=" + validUntil +
			'}';
	}

}
