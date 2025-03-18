package com.example.hellospring.service;

import java.io.IOException;
import java.math.BigDecimal;

public interface ExRateProvider {
	// 인터페이스는 기본적으로 메소드가 public 임

	BigDecimal getExRate(String currency) throws IOException;

}
