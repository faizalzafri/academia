package com.academia.platform.service;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import com.academia.platform.dto.Mail;

@Service
public class EmailService {

	private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

	@Autowired
	private JavaMailSender emailSender;

	@Autowired
	private SpringTemplateEngine templateEngine;

	@Value("${spring.mail.username:no-reply@example.com}")
	private String fromAddress;

	public void sendSimpleMessage(Mail mail) throws MessagingException {
		MimeMessage message = emailSender.createMimeMessage();

		MimeMessageHelper helper = new MimeMessageHelper(message,
				MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
				StandardCharsets.UTF_8.name());

		Context context = new Context();
		context.setVariables(mail.getModel());
		String html = templateEngine.process("email-template.html", context);

		helper.setTo(mail.getTo());
		helper.setText(html, true);
		helper.setSubject(mail.getSubject());
		helper.setFrom(mail.getFrom());

		emailSender.send(message);
		logger.info("Email sent to {}", mail.getTo());
	}

	public Mail createMail(String to, Map<String, Object> model) {
		Mail mail = new Mail();
		mail.setFrom(fromAddress);
		mail.setTo(to);
		mail.setSubject("New Announcement");
		mail.setModel(model);
		return mail;
	}
}
