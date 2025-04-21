package com.loop.api.modules.auth.service;

import com.loop.api.common.constants.ApiRoutes;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Slf4j
@Service
public class EmailService {

	private final JavaMailSender mailSender;
	private final String baseUrl;
	private final String contextPath;
	private final SpringTemplateEngine templateEngine;

	public EmailService(JavaMailSender mailSender,
						@Value("${app.base-url}") String baseUrl,
						@Value("${server.servlet.context-path}") String contextPath,
						SpringTemplateEngine templateEngine) {
		this.mailSender = mailSender;
		this.baseUrl = baseUrl;
		this.contextPath = contextPath;
		this.templateEngine = templateEngine;
	}

	@Async
	public void sendVerificationEmail(String to, String firstName, String token) {
		String link = baseUrl + contextPath + ApiRoutes.Auth.VERIFY + "?token=" + token;

		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

			helper.setTo(to);
			helper.setSubject("Confirm your email");

			Context thymeleafContext = new Context();
			thymeleafContext.setVariable("link", link);
			thymeleafContext.setVariable("firstName", firstName);

			String htmlContent = templateEngine.process("verification-email", thymeleafContext);
			helper.setText(htmlContent, true);

			ClassPathResource imageResource = new ClassPathResource("static/images/logo.png");

			helper.addInline("loop-logo", imageResource, "image/png");


			mailSender.send(message);
			log.info("✅ Verification email sent to {}", to);

		} catch (Exception e) {
			log.error("❌ Failed to send verification email to {}", to, e);
		}
	}
}
