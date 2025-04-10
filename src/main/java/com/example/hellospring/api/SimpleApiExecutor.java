package com.example.hellospring.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service
public class SimpleApiExecutor implements ApiExecutor{

	@Override
	public String execute (URI uri) throws IOException {
		String response;
		HttpURLConnection urlConnection = (HttpURLConnection)uri.toURL().openConnection();

		// java 가 직접 close 를 해준다 -> autoClosable -> autoClosable 구현체를 구현하고 있으면 자동으로 resource 를 닫아준다
		try (BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()))) {
			response = br.lines().collect(Collectors.joining());
		}
		return response;
	}
}
