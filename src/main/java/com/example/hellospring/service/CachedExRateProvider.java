package com.example.hellospring.service;

import java.io.IOException;
import java.math.BigDecimal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class CachedExRateProvider implements ExRateProvider{
	private final ExRateProvider target;

	private BigDecimal cachedExRate;

	@Override
	public BigDecimal getExRate (String currency) throws IOException {
		if(cachedExRate == null) {
			cachedExRate = this.target.getExRate(currency);
			log.info("Cache Updated");
		}

		return cachedExRate;
	}

}
