package com.loop.api.modules.auth.service;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.EnableAsync;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

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

	@Test
	void shouldSendVerificationEmail() throws Exception {
		MimeMessage mimeMessage = new MimeMessage((Session) null);
		when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
		when(templateEngine.process(eq("verification-email"), any(Context.class)))
				.thenReturn("<html><body>Mocked Email Content</body></html>");

		emailService.sendVerificationEmail("test@example.com", "testuser", "abc123");

		Thread.sleep(200);

		// No exception = passed, but we can verify key behaviors:
		verify(mailSender).send(any(MimeMessage.class));
		verify(templateEngine).process(eq("verification-email"), any(Context.class));
	}

	@Test
	void shouldLogErrorWhenEmailSendFails() throws Exception {
		MimeMessage mimeMessage = new MimeMessage((Session) null);
		when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
		when(templateEngine.process(anyString(), any(Context.class)))
				.thenReturn("<html><body>Mocked Email</body></html>");

		doThrow(new MailSendException("SMTP error")).when(mailSender).send(any(MimeMessage.class));

		emailService.sendVerificationEmail("fail@example.com", "testuser", "xyz");

		Thread.sleep(200);

		verify(mailSender).send(any(MimeMessage.class));
	}
}
