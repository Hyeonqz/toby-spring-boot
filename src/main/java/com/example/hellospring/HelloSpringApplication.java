package com.example.hellospring;

import java.io.IOException;
import java.math.BigDecimal;

import com.example.hellospring.domain.Payment;
import com.example.hellospring.service.PaymentService;
import com.example.hellospring.service.SimpleExRatePaymentService;
import com.example.hellospring.service.WebApiExRatePaymentService;

//@SpringBootApplication
public class HelloSpringApplication {

	public static void main (String[] args) throws IOException {
		// 		SpringApplication.run(HelloSpringApplication.class, args);
		PaymentService paymentService = new WebApiExRatePaymentService();
		Payment payment = paymentService.prepare(100L, "USD", BigDecimal.valueOf(50.7));
		System.out.println(payment.toString());

		PaymentService paymentService2 = new SimpleExRatePaymentService();
		Payment payment2 = paymentService2.prepare(100L, "USD", BigDecimal.valueOf(50.7));
		System.out.println(payment2.toString());
	}

}
