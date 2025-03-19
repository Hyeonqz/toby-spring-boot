package com.example.hellospring;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Sort {
	public static void main (String[] args) {
		List<Integer> scores = Arrays.asList(1,56,562,62,2,5,7);
		Collections.sort(scores);

		List<String> strings = Arrays.asList("Java","MySQL","asp","Spring");
		Collections.sort(strings, (o1, o2) -> o1.length() - o2.length());

		scores.forEach(System.out::println);
		strings.forEach(System.out::println);
	}
}
