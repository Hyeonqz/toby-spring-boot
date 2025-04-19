package com.example.hellospring;

import java.math.BigDecimal;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.example.hellospring.config.DataConfig;
import com.example.hellospring.order.Order;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

public class DataClient {
	public static void main (String[] args) {
		BeanFactory beanFactory = new AnnotationConfigApplicationContext(DataConfig.class);
		EntityManagerFactory emf = beanFactory.getBean(EntityManagerFactory.class);

		// em 생성
		EntityManager em =  emf.createEntityManager();
		// transaction 생성
		em.getTransaction().begin();

		// em.persist() -> 영속화 해달라 -> 영속성 컨텍스트로 들어감
		Order order = new Order("100", BigDecimal.valueOf(1000));
		em.persist(order);

		System.out.println(order);

		// 실제 db 에 commit
		em.getTransaction().commit();
		System.out.println(order);
		// em close
		em.close();
	}
}
