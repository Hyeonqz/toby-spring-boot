package com.example.hellospring.order;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Table("orders")
@Entity
public class Order {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String no;

	private BigDecimal total;

	// jakarta persistence api -> jpa
}
