package com.example.hellospring.exrate.impl;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.example.hellospring.api.ApiTemplate;
import com.example.hellospring.payment.ExRateProvider;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class WebApiExRateProvider implements ExRateProvider {
	private static final String URL = "https://open.er-api.com/v6/latest/";
	// 스프링 컨테이너 안에 싱글톤 빈으로 만들어서 전역에서 사용할 수 있게 한다. -> 한번 만들어 놓고 사용할 수 있다 -> 빈 처리, 매번 만들어야 한다? 빈 X
	// spring 이 구성정보와 bean 클래스를 결합해서 동작하는 어플리케이션을 만든다.
	private final ApiTemplate template;

	public WebApiExRateProvider (ApiTemplate template) {
		this.template = template;
	}

	@Override
	public BigDecimal getExRate (String currency) {
		String url = URL + currency;

		return template.getForExRate(url);
	}

}
