package com.example.hellospring;

import java.math.BigDecimal;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import com.example.hellospring.config.DataConfig;
import com.example.hellospring.data.OrderRepository;
import com.example.hellospring.order.Order;

@Component
public class DataClient {
	public static void main (String[] args) {
		BeanFactory beanFactory = new AnnotationConfigApplicationContext(DataConfig.class);
		OrderRepository orderRepository = beanFactory.getBean(OrderRepository.class);
		JpaTransactionManager transactionManager = beanFactory.getBean(JpaTransactionManager.class);

		try {
			new TransactionTemplate(transactionManager).execute(status -> {
				orderRepository.save(new Order("1000", BigDecimal.TEN));
				orderRepository.save(new Order("1000", BigDecimal.TEN));

				return null;
				}
			);
		} catch (DataIntegrityViolationException e) {
			System.out.println("주문번호 중복 복구 작업");
		}

	}

}
