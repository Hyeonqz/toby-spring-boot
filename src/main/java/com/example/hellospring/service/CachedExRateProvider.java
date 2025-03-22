package com.example.hellospring.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class CachedExRateProvider implements ExRateProvider{
	private final ExRateProvider target;

	private BigDecimal cachedExRate;
	private LocalDateTime cacheExpiryTime;

	@Override
	public BigDecimal getExRate (String currency) throws IOException {
		if(cachedExRate == null || cacheExpiryTime.isBefore(LocalDateTime.now())) {
			cachedExRate = this.target.getExRate(currency);
			cacheExpiryTime = LocalDateTime.now().plusSeconds(3);
			log.info("Cache Updated");
		}

		return cachedExRate;
	}

}
