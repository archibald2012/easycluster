package org.easycluster.easycluster.monitor.service;

import java.util.Map;

import javax.mail.MessagingException;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.VelocityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.ui.velocity.VelocityEngineUtils;

public class MailServiceImpl {

	private static final Logger	logger		= LoggerFactory.getLogger(MailServiceImpl.class);

	private JavaMailSender		mailSender;

	private VelocityEngine		velocityEngine;

	private String				from		= "spider@taotaosou.com";

	private String				encoding	= "utf-8";

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public void setMailSender(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}

	public JavaMailSender getMailSender() {
		return mailSender;
	}

	public void setVelocityEngine(VelocityEngine velocityEngine) {
		this.velocityEngine = velocityEngine;
	}

	public void sendVelocityMessage(MailInfo info, String templateName, Map<String, Object> model) {
		try {
			MimeMessageHelper mmmHelper = new MimeMessageHelper(getMailSender().createMimeMessage(), getEncoding());

			String result = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, templateName, model);
			String from = (info.getFrom() != null && info.getFrom().trim().length() > 0) ? info.getFrom() : this.getFrom();

			mmmHelper.setFrom(from);
			mmmHelper.setTo(info.getTo());
			mmmHelper.setSubject(info.getSubject());
			mmmHelper.setText(result, true);

			mailSender.send(mmmHelper.getMimeMessage());

		} catch (VelocityException e) {
			logger.error(e.getMessage());
			throw new RuntimeException(e);

		} catch (MessagingException e) {
			logger.error(e.getMessage());
			throw new RuntimeException(e);
		}
	}

	public void sendMessage(MailInfo info) {
		try {

			MimeMessageHelper mmmHelper = new MimeMessageHelper(getMailSender().createMimeMessage(), getEncoding());

			String from = (info.getFrom() != null && info.getFrom().trim().length() > 0) ? info.getFrom() : this.getFrom();

			mmmHelper.setFrom(from);
			mmmHelper.setTo(info.getTo());
			mmmHelper.setSubject(info.getSubject());
			mmmHelper.setText(info.getContent(), true);

			mailSender.send(mmmHelper.getMimeMessage());

		} catch (MessagingException e) {
			logger.error(e.getMessage());
			throw new RuntimeException(e);

		}
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}
}
