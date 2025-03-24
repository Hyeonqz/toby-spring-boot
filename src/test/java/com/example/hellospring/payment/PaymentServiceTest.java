package com.example.hellospring.payment;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.hellospring.exrate.impl.WebApiExRateProvider;

class PaymentServiceTest {

	@Test
	@DisplayName("prepare 메소드가 요구사항 3가지를 잘 충족했는지 검증한다")
	void prepare_test() throws IOException {
	    // given
		PaymentService paymentService = new PaymentService(new WebApiExRateProvider());

		// when
		Payment payment = paymentService.prepare(1L, "USD", BigDecimal.TEN);

		// then
		assertThat(payment.getExRate()).isNotNull();
		assertThat(payment.getConvertedAmount()).isEqualTo(payment.getExRate().multiply(payment.getForeignCurrencyAmount()));
		assertThat(payment.getCurrency()).isEqualTo("USD");
		assertThat(payment.getValidUntil()).isAfter(LocalDateTime.now());
		assertThat(payment.getValidUntil()).isBefore(LocalDateTime.now().plusMinutes(30));
	}

}