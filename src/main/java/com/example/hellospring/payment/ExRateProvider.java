package com.example.hellospring.payment;

import java.io.IOException;
import java.math.BigDecimal;

public interface ExRateProvider {
	// 인터페이스는 기본적으로 메소드가 public 임

	// 인터페이스를 사용하는 쪽으로 옮긴다 (기존 패키지: exrate -> 변경 패키지: payment)
	BigDecimal getExRate(String currency) throws IOException;

}
