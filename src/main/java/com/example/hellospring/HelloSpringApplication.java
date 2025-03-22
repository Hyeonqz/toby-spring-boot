package com.example.hellospring;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

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

	public static void main (String[] args) throws IOException, InterruptedException {
		BeanFactory beanFactory = new AnnotationConfigApplicationContext(ObjectFactory.class);
		PaymentService paymentService = beanFactory.getBean(PaymentService.class);

		Payment payment = paymentService.prepare(100L, "USD", BigDecimal.valueOf(50.7));
		System.out.println(payment.toString());

		System.out.println("\n");
		TimeUnit.SECONDS.sleep(1);

		Payment payment2 = paymentService.prepare(100L, "USD", BigDecimal.valueOf(50.7));
		System.out.println(payment2.toString());

		System.out.println("\n");

		TimeUnit.SECONDS.sleep(3);

		Payment payment3 = paymentService.prepare(100L, "USD", BigDecimal.valueOf(50.7));
		System.out.println(payment3);
	}

}
