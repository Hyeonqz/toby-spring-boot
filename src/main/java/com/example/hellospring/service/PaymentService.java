package com.example.hellospring.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

import com.example.hellospring.domain.ExRateData;
import com.example.hellospring.domain.Payment;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PaymentService {

	public Payment prepare(Long orderId, String currency, BigDecimal foreignCurrencyAmount) throws
		IOException {
		// 환율 가져오기
		URL url = new URL("https://open.er-api.com/v6/latest/" + currency);
		HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();

		BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

		String response = br.lines().collect(Collectors.joining());

		ObjectMapper mapper = new ObjectMapper();
		ExRateData exRateData = mapper.readValue(response, ExRateData.class);

		BigDecimal krw = exRateData.rates().get("KRW");
		String result = exRateData.result();

		// 금액 계산
		BigDecimal convertedAmount = foreignCurrencyAmount.multiply(krw);

		// 유효 시간 계산
		LocalDateTime validUntil = LocalDateTime.now().plusMinutes(30);

		br.close();
		// 유효 시간 계산

		return new Payment(orderId, currency, foreignCurrencyAmount, krw, convertedAmount, validUntil);
	}

	public static void main (String[] args) throws IOException {
		PaymentService paymentService = new PaymentService();
		Payment payment = paymentService.prepare(100L, "USD", BigDecimal.valueOf(50.7));
		System.out.println(payment.toString());
	}

}
