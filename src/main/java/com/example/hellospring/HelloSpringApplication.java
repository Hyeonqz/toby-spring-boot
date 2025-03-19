package com.example.hellospring;

import java.io.IOException;
import java.math.BigDecimal;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.example.hellospring.domain.Payment;
import com.example.hellospring.service.ObjectFactory;
import com.example.hellospring.service.PaymentService;

//@SpringBootApplication
public class HelloSpringApplication {
/*	public static void main (String[] args) {
		SpringApplication.run(HelloSpringApplication.class, args);
	}*/

	public static void main (String[] args) throws IOException {
		BeanFactory beanFactory = new AnnotationConfigApplicationContext(ObjectFactory.class);
		PaymentService paymentService = beanFactory.getBean(PaymentService.class);
		PaymentService paymentService2 = beanFactory.getBean(PaymentService.class);

		System.out.println(paymentService);
		System.out.println(paymentService2);
		System.out.println(paymentService == paymentService2);

		Payment payment = paymentService.prepare(100L, "USD", BigDecimal.valueOf(50.7));
		System.out.println(payment.toString());
	}

}
