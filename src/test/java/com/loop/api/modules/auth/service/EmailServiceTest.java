package com.loop.api.modules.auth.service;

import com.loop.api.common.exception.EmailSendException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Tag("UnitTest")
@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

	@Mock
	private JavaMailSender mailSender;

	private EmailService emailService;

	@BeforeEach
	void setup() {
		emailService = new EmailService(mailSender, "https://app.com", "/api/v1");
	}

	@Test
	void shouldSendVerificationEmail() {
		ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

		emailService.sendVerificationEmail("test@example.com", "abc123");

		verify(mailSender).send(captor.capture());

		SimpleMailMessage message = captor.getValue();
		assertEquals("test@example.com", message.getTo()[0]);
		assertEquals("Confirm your email", message.getSubject());
		assertTrue(message.getText().contains("https://app.com/api/v1/auth/verify?token=abc123"));
	}

	@Test
	void shouldThrowWhenEmailSendFails() {
		doThrow(new MailSendException("SMTP error")).when(mailSender).send(any(SimpleMailMessage.class));

		EmailSendException exception = assertThrows(
				EmailSendException.class,
				() -> emailService.sendVerificationEmail("fail@example.com", "xyz")
		);

		assertTrue(exception.getMessage().contains("fail@example.com"));
	}
}
