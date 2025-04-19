package com.example.hellospring.order;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;

@Table(name= "orders")
@Entity
public class Order {
	// jakarta persistence api -> jpa

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true)
	private String no;

	private BigDecimal total;

	@Builder
	public Order (String no, BigDecimal total) {
		this.no = no;
		this.total = total;
	}

	// jpa 사용시 기본 생성자는 꼭 만들어 줘야 한다.
	public Order () {
	}

	@Override
	public String toString () {
		return "Order{" +
			"id=" + id +
			", no='" + no + '\'' +
			", total=" + total +
			'}';
	}

}
