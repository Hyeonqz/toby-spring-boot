package com.example.hellospring.exrate.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.stereotype.Component;

import com.example.hellospring.api.ApiExecutor;
import com.example.hellospring.api.ErApiExRateExtractor;
import com.example.hellospring.api.ExRateExtractor;
import com.example.hellospring.api.SimpleApiExecutor;
import com.example.hellospring.exrate.ExRateData;
import com.example.hellospring.payment.ExRateProvider;
import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class WebApiExRateProvider implements ExRateProvider {
	private static final String URL = "https://open.er-api.com/v6/latest/";

	@Override
	public BigDecimal getExRate (String currency) {
		String url =  URL + currency;
		// 메소드 주입은 Bean 으로 작성하는게 아닌 우리가 작성하는 로직에서 작성해야 한다.

		return this.runApiForExRate(url, new SimpleApiExecutor(), new ErApiExRateExtractor());
	}

	// 1. 재사용성 증가를 위해서 메소드 추출을 한다.
	private BigDecimal runApiForExRate (String url, ApiExecutor apiExecutor, ExRateExtractor exRateExtractor) {
		URI uri;
		try {
			uri = new URI(url);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}

		String response;
		try {
			response = apiExecutor.execute(uri);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		ExRateData exRateData;
		try {
			return exRateExtractor.extract(response);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

}
