package com.example.hellospring.api;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;

import com.example.hellospring.exrate.ExRateData;
import com.fasterxml.jackson.core.JsonProcessingException;

public class ApiTemplate {
	private final ApiExecutor apiExecutor;
	private final ExRateExtractor exRateExtractor;

	public ApiTemplate () {
		this.apiExecutor = new HttpClientApiExecutor();
		this.exRateExtractor = new ErApiExRateExtractor();
	}

	public ApiTemplate (ApiExecutor apiExecutor, ExRateExtractor exRateExtractor) {
		this.apiExecutor = new HttpClientApiExecutor();
		this.exRateExtractor = new ErApiExRateExtractor();
	}

	// 0. 아래 3가지는 템플릿을 지원하는 디폴트 콜백 3가지 메소드이다.
	public BigDecimal getForExRate(String url) {
		return this.getForExRate(url, this.apiExecutor, this.exRateExtractor);
	}

	public BigDecimal getForExRate(String url, ApiExecutor apiExecutor) {
		return this.getForExRate(url, apiExecutor, this.exRateExtractor);
	}

	public BigDecimal getForExRate(String url, ExRateExtractor exRateExtractor) {
		return this.getForExRate(url, this.apiExecutor, exRateExtractor);
	}

	// 1. 템플릿 메소드는 아래 1개이다.
	public BigDecimal getForExRate (String url, ApiExecutor apiExecutor, ExRateExtractor exRateExtractor) {
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
