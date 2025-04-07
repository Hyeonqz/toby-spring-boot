package com.example.hellospring.exrate.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.example.hellospring.exrate.ExRateData;
import com.example.hellospring.payment.ExRateProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class WebApiExRateProvider implements ExRateProvider {

	@Override
	public BigDecimal getExRate (String currency) {
		// checked Exception -> unCheckedException
		String url = "https://open.er-api.com/v6/latest/" + currency;

		URI uri;
		try {
			uri = new URI(url);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}

		String response;
		try {
			HttpURLConnection urlConnection = (HttpURLConnection)uri.toURL().openConnection();

			// java 가 직접 close 를 해준다 -> autoClosable -> autoClosable 구현체를 구현하고 있으면 자동으로 resource 를 닫아준다
			try (BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()))) {
				response = br.lines().collect(Collectors.joining());
				// br.close() 위 메소드가 필요가 없어진다.
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}


		ExRateData exRateData;
		try {
			ObjectMapper mapper = new ObjectMapper();
			exRateData = mapper.readValue(response, ExRateData.class);

			return exRateData.rates().get("KRW");
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

}
