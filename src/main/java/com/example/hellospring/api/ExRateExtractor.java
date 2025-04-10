package com.example.hellospring.api;

import java.math.BigDecimal;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface ExRateExtractor {
	BigDecimal extract(String response) throws JsonProcessingException;
}
