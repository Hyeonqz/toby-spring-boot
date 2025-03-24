package com.example.hellospring.payment;

import static java.math.BigDecimal.*;
import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.example.hellospring.TestObjectFactory;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestObjectFactory.class) // 구성정보 클래스를 지정한다.
class PaymentServiceTest {

	@Autowired private PaymentService paymentService;
	@Autowired private ExRateProviderStub exRateProviderStub;


	@Test
	@DisplayName("prepare 메소드가 요구사항 3가지를 잘 충족했는지 검증한다")
	void converted_Amount() throws IOException {
	    // given
		Payment payment = paymentService.prepare(1L, "USD", TEN);

		assertThat(payment.getExRate()).isEqualByComparingTo(valueOf(1_000));
		assertThat(payment.getConvertedAmount()).isEqualByComparingTo(valueOf(10_000));

	}

	@NonNull
	private void getPayment (BigDecimal exRate, BigDecimal convertedAmount) throws IOException {
		PaymentService paymentService = new PaymentService(new ExRateProviderStub(exRate));

		// when
		Payment payment = paymentService.prepare(1L, "USD", TEN);

		// then
		assertThat(payment.getExRate()).isEqualByComparingTo(exRate);
		assertThat(payment.getConvertedAmount()).isEqualTo(convertedAmount);
		assertThat(payment.getCurrency()).isEqualTo("USD");
	}

}