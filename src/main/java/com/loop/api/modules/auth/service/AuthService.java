package com.loop.api.modules.auth.service;

import com.loop.api.common.exception.*;
import com.loop.api.common.util.UserValidationUtil;
import com.loop.api.modules.auth.dto.*;
import com.loop.api.modules.auth.model.PasswordResetCode;
import com.loop.api.modules.auth.model.RefreshToken;
import com.loop.api.modules.auth.model.VerificationToken;
import com.loop.api.modules.auth.repository.PasswordResetCodeRepository;
import com.loop.api.modules.auth.repository.VerificationTokenRepository;
import com.loop.api.modules.user.model.AuthProvider;
import com.loop.api.modules.user.model.User;
import com.loop.api.modules.user.model.UserOAuth;
import com.loop.api.modules.user.repository.UserOAuthRepository;
import com.loop.api.modules.user.repository.UserRepository;
import com.loop.api.security.JwtTokenProvider;
import com.loop.api.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;
	private final RefreshTokenService refreshTokenService;
	private final AuthenticationManager authenticationManager;
	private final EmailService emailService;
	private final VerificationTokenRepository verificationTokenRepository;
	private final PasswordResetCodeRepository passwordResetCodeRepository;
	private final GoogleOAuthService googleOAuthService;
	private final UserOAuthRepository userOAuthRepository;

	@Value("${app.verification.token-expiration-hours}")
	private int verificationTokenExpiryHours;

	public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
					   JwtTokenProvider jwtTokenProvider, RefreshTokenService refreshTokenService,
					   AuthenticationManager authenticationManager,
					   EmailService emailService,
					   VerificationTokenRepository verificationTokenRepository,
					   PasswordResetCodeRepository passwordResetCodeRepository,
					   GoogleOAuthService googleOAuthService, UserOAuthRepository userOAuthRepository) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtTokenProvider = jwtTokenProvider;
		this.refreshTokenService = refreshTokenService;
		this.authenticationManager = authenticationManager;
		this.emailService = emailService;
		this.verificationTokenRepository = verificationTokenRepository;
		this.passwordResetCodeRepository = passwordResetCodeRepository;
		this.googleOAuthService = googleOAuthService;
		this.userOAuthRepository = userOAuthRepository;
	}

	public boolean isEmailRegistered(String email) {
		try {
			UserValidationUtil.validateUniqueUserFields(email, null, null, userRepository, null);
			return false;
		} catch (UserAlreadyExistsException e) {
			return true;
		}
	}

	public String registerUser(RegisterRequest request) {
		UserValidationUtil.validateUniqueUserFields(
				request.getEmail(), request.getUsername(), null, userRepository, null);

		try {
			User user = new User();
			user.setEmail(request.getEmail());
			user.setUsername(request.getUsername());
			user.setPassword(passwordEncoder.encode(request.getPassword()));
			userRepository.save(user);

			String token = UUID.randomUUID().toString();
			VerificationToken vt = new VerificationToken();
			vt.setToken(token);
			vt.setUser(user);
			vt.setExpiryDate(Instant.now().plus(Duration.ofHours(verificationTokenExpiryHours)));
			verificationTokenRepository.save(vt);

			emailService.sendVerificationEmail(user.getEmail(), user.getUsername(), token);
			return "User registered successfully";
		} catch (Exception e) {
			throw new RuntimeException("Error registering user: " + e.getMessage());
		}
	}

	public void verifyEmailToken(String token) {
		VerificationToken vt = verificationTokenRepository.findByToken(token)
				.orElseThrow(() -> new InvalidTokenException("Invalid token"));

		if (vt.getExpiryDate().isBefore(Instant.now())) {
			throw new InvalidTokenException("Verification token has expired");
		}

		User user = vt.getUser();
		user.setVerified(true);
		userRepository.save(user);

		verificationTokenRepository.delete(vt);
	}

	public void resendVerificationEmail(String email) {
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new UserNotFoundException("User not found"));

		if (user.isVerified()) {
			throw new UserAlreadyVerifiedException("User is already verified");
		}

		verificationTokenRepository.deleteByUser(user);

		VerificationToken token = new VerificationToken();
		token.setToken(UUID.randomUUID().toString());
		token.setUser(user);
		token.setExpiryDate(Instant.now().plus(Duration.ofHours(verificationTokenExpiryHours)));
		verificationTokenRepository.save(token);

		emailService.sendVerificationEmail(user.getEmail(), user.getUsername(), token.getToken());
	}

	public LoginResponse loginUser(LoginRequest request) {
		// First, check if the user exists
		Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
		if (userOpt.isEmpty()) {
			throw new UserNotFoundException("This email is not registered. Would you like to sign up instead?");
		}

		try {
			Authentication authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(
							request.getEmail(),
							request.getPassword()
					)
			);

			// Authentication succeeded, get UserPrincipal
			UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

			// Now generate JWT using their email or username
			String accessToken = jwtTokenProvider.generateToken(userPrincipal);

			// Generate refresh token and save to DB
			RefreshToken refreshToken = refreshTokenService.createRefreshToken(userPrincipal.getId());

			return LoginResponse.builder()
					.userId(userPrincipal.getId())
					.accessToken(accessToken)
					.refreshToken(refreshToken.getToken())
					.build();

		} catch (AuthenticationException ex) {
			throw new InvalidCredentialsException("Invalid email or password");
		}
	}

	public LoginResponse refreshAccessToken(String refreshTokenStr) {
		// Validate the refresh token and fetch user details
		RefreshToken oldToken = refreshTokenService.verifyRefreshToken(refreshTokenStr);
		if (oldToken == null) {
			throw new InvalidTokenException("Refresh token is invalid or expired");
		}
		User user = oldToken.getUser();

		// Delete old refresh token and generate a new one
		refreshTokenService.deleteByToken(oldToken.getToken());
		RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user.getId());

		// Generate new access token
		UserPrincipal userPrincipal = new UserPrincipal(user);
		String newAccessToken = jwtTokenProvider.generateToken(userPrincipal);

		// Return the login response with new tokens
		return LoginResponse.builder()
				.userId(user.getId())
				.accessToken(newAccessToken)
				.refreshToken(newRefreshToken.getToken())
				.build();
	}

	public void logout(String refreshTokenStr) {
		// Validate the refresh token
		RefreshToken refreshToken = refreshTokenService.verifyRefreshToken(refreshTokenStr);
		if (refreshToken == null) {
			throw new InvalidTokenException("Invalid or expired refresh token.");
		}

		// Delete the refresh token from the database to log the user out
		refreshTokenService.deleteByToken(refreshToken.getToken());
	}

	@Transactional
	public void sendPasswordResetEmail(String email) {
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new UserNotFoundException("This email is not registered"));

		passwordResetCodeRepository.deleteByUser(user);

		SecureRandom secureRandom = new SecureRandom();
		String code = String.format("%06d", secureRandom.nextInt(1_000_000));

		PasswordResetCode passwordResetCode = new PasswordResetCode();
		passwordResetCode.setCode(code);
		passwordResetCode.setUser(user);
		passwordResetCode.setExpiryDate(Instant.now().plus(Duration.ofMinutes(15)));

		passwordResetCodeRepository.save(passwordResetCode);

		emailService.sendResetPasswordEmail(user.getEmail(), user.getUsername(), code);
	}

	private PasswordResetCode getValidResetCode(String email, String code) {
		return passwordResetCodeRepository.findByUserEmailAndCode(email, code)
				.filter(c -> c.getExpiryDate().isAfter(Instant.now()))
				.orElseThrow(() -> new InvalidTokenException("Invalid reset code"));
	}

	public void verifyResetCode(String email, String code) {
		getValidResetCode(email, code); // Just validate
	}

	public void resetPassword(String email, String code, String newPassword) {
		PasswordResetCode resetCode = getValidResetCode(email, code);

		User user = resetCode.getUser();
		user.setPassword(passwordEncoder.encode(newPassword));
		userRepository.save(user);

		passwordResetCodeRepository.delete(resetCode);
	}

	@Transactional
	public LoginResponse oauthLogin(OAuthLoginRequest request) {
		if (!"google".equalsIgnoreCase(request.getProvider())) {
			throw new UnsupportedOperationException("Only Google login is supported for now.");
		}

		GoogleTokenResponse tokenResponse = googleOAuthService.exchangeCodeForTokens(request.getCode(),
				request.getRedirectUri());

		GoogleUserInfo userInfo = googleOAuthService.getUserInfo(tokenResponse.getIdToken());

		Optional<User> existingUserOpt = userRepository.findByEmail(userInfo.getEmail());

		User user;
		if (existingUserOpt.isPresent()) {
			user = existingUserOpt.get();

			Optional<UserOAuth> existingConnection = userOAuthRepository.findByUserIdAndProvider(user.getId(),
					AuthProvider.GOOGLE);

			if (existingConnection.isEmpty()) {
				UserOAuth userOAuth = new UserOAuth();
				userOAuth.setUser(user);
				userOAuth.setProvider(AuthProvider.GOOGLE);
				userOAuth.setProviderId(userInfo.getSub());
				userOAuthRepository.save(userOAuth);
			}
		} else {
			user = createNewUser(userInfo);
		}

		Long userId = user.getId();
		String accessToken = jwtTokenProvider.generateToken(user);
		String refreshToken = refreshTokenService.createRefreshToken(userId).getToken();

		return new LoginResponse(userId, accessToken, refreshToken);
	}

	private User createNewUser(GoogleUserInfo userInfo) {
		User user = new User();
		user.setEmail(userInfo.getEmail());
		user.setUsername(userInfo.getName());
		user.setVerified(userInfo.isEmailVerified());
		user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
		user.setProfileUrl(userInfo.getPicture());

		user = userRepository.save(user);

		UserOAuth userOAuth = new UserOAuth();
		userOAuth.setUser(user);
		userOAuth.setProvider(AuthProvider.GOOGLE);
		userOAuth.setProviderId(userInfo.getSub());
		userOAuthRepository.save(userOAuth);

		return user;
	}
}
