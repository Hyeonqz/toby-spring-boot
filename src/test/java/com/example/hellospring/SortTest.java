package com.example.hellospring;

import java.util.Arrays;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class SortTest {
	private Sort sort;

	@BeforeEach
	void beforeEach () {
		sort = new Sort();
		System.out.println(this);
	}

	@Test
	@DisplayName("BDD 스타일 테스트 => 길이 순으로 정렬을 한다")
	void Sort_Length_Test() {
	    // given

		// when
		List<String> list = sort.sortByLength(Arrays.asList("aa", "b", "ccc"));

	    // then
		Assertions.assertThat(list).isEqualTo(List.of("b","aa","ccc"));
	}

	@Test
	@DisplayName("BDD 스타일 테스트 => 길이 순으로 정렬을 한다")
	void Sort_Already_Test() {
		// given

		// when
		List<String> list = sort.sortByLength(Arrays.asList("b", "aa", "ccc"));

		// then
		Assertions.assertThat(list).isEqualTo(List.of("b","aa","ccc"));
	}

}
