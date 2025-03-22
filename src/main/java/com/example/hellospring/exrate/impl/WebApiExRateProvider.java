package com.example.hellospring.exrate.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.example.hellospring.exrate.ExRateData;
import com.example.hellospring.payment.ExRateProvider;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class WebApiExRateProvider implements ExRateProvider {

	@Override
	public BigDecimal getExRate (String currency) throws IOException {
		URL url = new URL("https://open.er-api.com/v6/latest/" + currency);
		HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
		BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
		String response = br.lines().collect(Collectors.joining());
		br.close();

		ObjectMapper mapper = new ObjectMapper();
		ExRateData exRateData = mapper.readValue(response, ExRateData.class);

		log.info("API ExRate : {}", exRateData.rates().get("KRW"));

		return exRateData.rates().get("KRW");
	}

}
