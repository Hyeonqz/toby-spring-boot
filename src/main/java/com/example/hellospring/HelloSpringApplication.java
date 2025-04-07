package com.example.hellospring;

import java.math.BigDecimal;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.example.hellospring.payment.Payment;
import com.example.hellospring.payment.PaymentService;

//@SpringBootApplication
public class HelloSpringApplication {
/*	public static void main (String[] args) {
		SpringApplication.run(HelloSpringApplication.class, args);
	}*/

	public static void main (String[] args) {
		BeanFactory beanFactory = new AnnotationConfigApplicationContext(PaymentConfig.class);
		PaymentService paymentService = beanFactory.getBean(PaymentService.class);

		Payment payment = paymentService.prepare(100L, "USD", BigDecimal.valueOf(50.7));
		System.out.println(payment.toString());
	}

}
