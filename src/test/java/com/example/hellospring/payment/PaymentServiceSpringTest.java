package com.example.hellospring.payment;

import static java.math.BigDecimal.*;
import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.example.hellospring.TestPaymentConfig;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestPaymentConfig.class)
public class PaymentServiceSpringTest {

	@Autowired PaymentService paymentService;
	@Autowired Clock clock;

	@Test
	void validUntil() throws IOException {
		Payment payment = paymentService.prepare(1L, "USD", TEN);

		LocalDateTime now = LocalDateTime.now(this.clock);
		LocalDateTime expectedValidUntil = now.plusMinutes(30);

		Assertions.assertThat(payment.getValidUntil()).isEqualTo(expectedValidUntil);
	}

	@NonNull
	private void getPayment (BigDecimal exRate, BigDecimal convertedAmount, Clock clock) throws IOException {
		PaymentService paymentService = new PaymentService(new ExRateProviderStub(exRate), clock);

		// when
		Payment payment = paymentService.prepare(1L, "USD", TEN);

		// then
		assertThat(payment.getExRate()).isEqualByComparingTo(exRate);
		assertThat(payment.getConvertedAmount()).isEqualTo(convertedAmount);
		assertThat(payment.getCurrency()).isEqualTo("USD");
	}

}
