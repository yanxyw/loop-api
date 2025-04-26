package com.loop.api.modules.auth.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.loop.api.modules.auth.dto.GoogleTokenResponse;
import com.loop.api.modules.auth.dto.GoogleUserInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Component
public class GoogleOAuthService {

	private final RestTemplate restTemplate = new RestTemplate();

	@Value("${google.client-id}")
	private String clientId;

	@Value("${google.client-secret}")
	private String clientSecret;

	public GoogleTokenResponse exchangeCodeForTokens(String code, String redirectUri) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("client_id", clientId);
		params.add("client_secret", clientSecret);
		params.add("code", code);
		params.add("grant_type", "authorization_code");
		params.add("redirect_uri", redirectUri);

		HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);

		return restTemplate.postForObject(
				"https://oauth2.googleapis.com/token",
				requestEntity,
				GoogleTokenResponse.class
		);
	}

	public GoogleUserInfo getUserInfo(String idToken) {
		DecodedJWT decodedJWT = JWT.decode(idToken);
		System.out.println(decodedJWT.getSubject());

		String sub = decodedJWT.getSubject();
		String email = decodedJWT.getClaim("email").asString();
		boolean emailVerified = decodedJWT.getClaim("email_verified").asBoolean();
		String name = decodedJWT.getClaim("name").asString();
		String picture = decodedJWT.getClaim("picture").asString();

		return new GoogleUserInfo(sub, email, emailVerified, name, picture);
	}
}
