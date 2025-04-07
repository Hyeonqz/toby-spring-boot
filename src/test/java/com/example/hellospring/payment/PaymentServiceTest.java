package com.example.hellospring.payment;

import static java.math.BigDecimal.*;
import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.example.hellospring.TestPaymentConfig;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestPaymentConfig.class) // 구성정보 클래스를 지정한다.
class PaymentServiceTest {
	Clock clock = null;

	@Autowired private PaymentService paymentService;

	@BeforeEach
	public void init() {
		this.clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
	}


	@Test
	@DisplayName("prepare 메소드가 요구사항 3가지를 잘 충족했는지 검증한다")
	void convertedAmount() {


	    // given
		Payment payment = paymentService.prepare(1L, "USD", TEN);

		getPayment(valueOf(500), valueOf(5_000), clock);
		getPayment(valueOf(1_000), valueOf(10_000), clock);
		getPayment(valueOf(10_000), valueOf(100_000), clock);
	}

	@Test
	void validUntil() {
		PaymentService paymentService1 = new PaymentService(new ExRateProviderStub(valueOf(1_000)), clock);

		Payment payment = paymentService1.prepare(1L, "USD", TEN);

		// valid until 이 prepare() 30분 뒤로 설정됐는가?
		LocalDateTime now = LocalDateTime.now(this.clock);
		LocalDateTime expectedValidUntil = now.plusMinutes(30);

		Assertions.assertThat(payment.getValidUntil()).isEqualTo(expectedValidUntil);
	}

	@NonNull
	private void getPayment (BigDecimal exRate, BigDecimal convertedAmount, Clock clock) {
		PaymentService paymentService = new PaymentService(new ExRateProviderStub(exRate), clock);

		// when
		Payment payment = paymentService.prepare(1L, "USD", TEN);

		// then
		assertThat(payment.getExRate()).isEqualByComparingTo(exRate);
		assertThat(payment.getConvertedAmount()).isEqualTo(convertedAmount);
		assertThat(payment.getCurrency()).isEqualTo("USD");
	}

}