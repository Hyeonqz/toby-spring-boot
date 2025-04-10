package com.example.hellospring.api;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.example.hellospring.exrate.ExRateData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ErApiExRateExtractor implements  ExRateExtractor{

	@Override
	public BigDecimal extract (String response) throws JsonProcessingException {
		ExRateData exRateData;
		ObjectMapper mapper = new ObjectMapper();
		exRateData = mapper.readValue(response, ExRateData.class);

		return exRateData.rates().get("KRW");
	}

}
