package com.loop.api.modules.auth.service;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.EnableAsync;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@Tag("UnitTest")
@ExtendWith(MockitoExtension.class)
@EnableAsync
class EmailServiceTest {

	@Mock
	private JavaMailSender mailSender;

	@Mock
	private SpringTemplateEngine templateEngine;

	private EmailService emailService;


	@BeforeEach
	void setup() {
		emailService = new EmailService(mailSender, "https://app.com", "/api/v1", templateEngine);
	}

	@Nested
	@DisplayName("Send Verification Email Tests")
	class SendVerificationEmailTests {
		@Test
		void shouldSendVerificationEmail() throws Exception {
			MimeMessage mimeMessage = new MimeMessage((Session) null);
			when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
			when(templateEngine.process(eq("verification-email"), any(Context.class)))
					.thenReturn("<html><body>Mocked Email Content</body></html>");

			emailService.sendVerificationEmail("test@example.com", "Testy", "abc123");

			Thread.sleep(200); // give time for async

			// Verify the template engine was used correctly
			ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
			verify(templateEngine).process(eq("verification-email"), contextCaptor.capture());

			Context capturedContext = contextCaptor.getValue();
			assertEquals("Testy", capturedContext.getVariable("firstName"));
			assertEquals("https://app.com/api/v1/auth/verify?token=abc123", capturedContext.getVariable("link"));

			// Also verify email was sent
			verify(mailSender).send(any(MimeMessage.class));
		}


		@Test
		void shouldLogErrorWhenEmailSendFails() throws Exception {
			MimeMessage mimeMessage = new MimeMessage((Session) null);
			when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
			when(templateEngine.process(anyString(), any(Context.class)))
					.thenReturn("<html><body>Mocked Email</body></html>");

			doThrow(new MailSendException("SMTP error"))
					.when(mailSender).send(any(MimeMessage.class));

			emailService.sendVerificationEmail("fail@example.com", "TestFail", "xyz");

			Thread.sleep(200); // wait for async

			verify(mailSender).send(any(MimeMessage.class));
			verify(templateEngine).process(eq("verification-email"), any(Context.class));
		}
	}

	@Nested
	@DisplayName("Reset Password Email Tests")
	class ResetPasswordEmailTests {

		@Test
		@DisplayName("Should send reset password email successfully")
		void shouldSendResetPasswordEmailSuccessfully() throws Exception {
			MimeMessage mimeMessage = new MimeMessage((Session) null);
			when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
			when(templateEngine.process(eq("reset-password-email"), any(Context.class)))
					.thenReturn("<html><body>Reset Email Content</body></html>");

			emailService.sendResetPasswordEmail("reset@example.com", "Resetty", "654321");

			// Delay for @Async execution (if not removed)
			Thread.sleep(200);

			ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
			verify(templateEngine).process(eq("reset-password-email"), contextCaptor.capture());

			Context capturedContext = contextCaptor.getValue();
			assertEquals("Resetty", capturedContext.getVariable("firstName"));
			assertEquals("654321", capturedContext.getVariable("code"));

			verify(mailSender).send(any(MimeMessage.class));
		}

		@Test
		@DisplayName("Should log error when reset password email fails to send")
		void shouldLogErrorWhenResetPasswordEmailFails() throws Exception {
			MimeMessage mimeMessage = new MimeMessage((Session) null);
			when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
			when(templateEngine.process(anyString(), any(Context.class)))
					.thenReturn("<html><body>Error Reset Email</body></html>");

			doThrow(new MailSendException("SMTP error")).when(mailSender).send(any(MimeMessage.class));

			emailService.sendResetPasswordEmail("fail@example.com", "FailUser", "000000");

			Thread.sleep(200); // wait for async if needed

			verify(mailSender).send(any(MimeMessage.class));
			verify(templateEngine).process(eq("reset-password-email"), any(Context.class));
		}
	}
}
