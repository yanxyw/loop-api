package com.loop.api.modules.auth.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.loop.api.modules.auth.dto.GoogleTokenResponse;
import com.loop.api.modules.auth.dto.GoogleUserInfo;
import com.loop.api.modules.auth.dto.OAuthLoginRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Component
public class GoogleOAuthService {

	private final RestTemplate restTemplate = new RestTemplate();

	@Value("${google.desktopClientId}")
	private String desktopClientId;

	@Value("${google.webClientId}")
	private String webClientId;

	@Value("${google.desktopClientSecret}")
	private String desktopClientSecret;

	private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

	public boolean isIdToken(String token) {
		try {
			DecodedJWT jwt = JWT.decode(token);

			String issuer = jwt.getIssuer();
			String audience = jwt.getAudience().isEmpty() ? null : jwt.getAudience().getFirst();
			String subject = jwt.getSubject();

			return issuer != null && audience != null && subject != null;
		} catch (JWTDecodeException e) {
			return false;
		}
	}

	public GoogleUserInfo verifyIdToken(String idTokenString) throws GeneralSecurityException, IOException {
		GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier
				.Builder(new NetHttpTransport(), JSON_FACTORY)
				.setAudience(Collections.singletonList(webClientId))
				.build();

		GoogleIdToken idToken = verifier.verify(idTokenString);
		if (idToken == null) {
			throw new IllegalArgumentException("Invalid ID token");
		}

		GoogleIdToken.Payload payload = idToken.getPayload();

		return new GoogleUserInfo(
                (String) payload.get("sub"),
				payload.getEmail(),
				payload.getEmailVerified(),
				(String) payload.get("name"),
				(String) payload.get("picture")
		);
	}

	public GoogleTokenResponse exchangeCodeForTokens(String code, String redirectUri) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("client_id", desktopClientId);
		params.add("client_secret", desktopClientSecret);
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

	public GoogleUserInfo decodeIdToken(String idToken) {
		DecodedJWT decodedJWT = JWT.decode(idToken);

		return new GoogleUserInfo(
				decodedJWT.getSubject(),
				decodedJWT.getClaim("email").asString(),
				decodedJWT.getClaim("email_verified").asBoolean(),
				decodedJWT.getClaim("name").asString(),
				decodedJWT.getClaim("picture").asString()
		);
	}

	public GoogleUserInfo getUserInfo(OAuthLoginRequest request) throws GeneralSecurityException, IOException {
		if (isIdToken(request.getCode())) {
			return verifyIdToken(request.getCode()); // Mobile flow
		} else {
			GoogleTokenResponse response = exchangeCodeForTokens(request.getCode(), request.getRedirectUri());
			return decodeIdToken(response.getIdToken()); // Web flow
		}
	}
}
