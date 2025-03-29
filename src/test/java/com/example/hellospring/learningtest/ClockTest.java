package com.example.hellospring.learningtest;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class ClockTest {

	// Clock 을 이용해서 LocalDateTime.now
	@Test
	void clock_test() {
		Clock clock = Clock.systemDefaultZone();

		LocalDateTime dt1 = LocalDateTime.now(clock);
		LocalDateTime dt2 = LocalDateTime.now(clock);

		Assertions.assertThat(dt1).isBefore(dt2);
		Assertions.assertThat(dt2).isAfter(dt1);

	}
	
	@Test
	void fixed_clock_test() {
	    // given
		// 현재 시간 기준으로 시계 고정
		Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

		LocalDateTime dt1 = LocalDateTime.now(clock);
		LocalDateTime dt2 = LocalDateTime.now(clock);

		LocalDateTime dt3 = LocalDateTime.now(clock).plusHours(12);

	    // when
		Assertions.assertThat(dt2).isEqualTo(dt1);
		Assertions.assertThat(dt3).isEqualTo(dt2.plusHours(12));
	    
	    // then
	}


	// Clock 을 Test 에서 사용 할 때 내가 원하는 시간을 지정해서 현재 시간을 가져올 수 있나?
}
