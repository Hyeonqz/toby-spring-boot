package com.example.hellospring;

import java.io.IOException;
import java.math.BigDecimal;

import com.example.hellospring.domain.Payment;
import com.example.hellospring.service.ObjectFactory;
import com.example.hellospring.service.PaymentService;

//@SpringBootApplication
public class HelloSpringApplication {

	public static void main (String[] args) throws IOException {
		// SpringApplication.run(HelloSpringApplication.class, args);
		ObjectFactory objectFactory = new ObjectFactory();
		PaymentService paymentService = objectFactory.paymentService();

		Payment payment = paymentService.prepare(100L, "USD", BigDecimal.valueOf(50.7));
		System.out.println(payment.toString());
	}

}
