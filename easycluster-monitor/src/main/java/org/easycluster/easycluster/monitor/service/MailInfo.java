package org.easycluster.easycluster.monitor.service;

import java.util.HashMap;
import java.util.Map;

public class MailInfo {

	private String				from;

	private String[]			to;

	private String[]			cc;

	private String[]			bcc;

	private String[]			replyTo;

	private String				subject;

	private String				content;

	private Map<String, Object>	mailAttrs	= new HashMap<String, Object>();

	public MailInfo() {
	}

	public MailInfo(String from, String[] to, String subject, String content) {
		this(from, to, null, null, null, subject, content);
	}

	public MailInfo(String from, String[] to, String[] cc, String[] bcc, String[] replyTo, String subject, String content) {
		this.from = from;
		this.to = to;
		this.cc = cc;
		this.bcc = bcc;
		this.replyTo = replyTo;
		this.subject = subject;
		this.content = content;
	}

	/**
	 * @return the mailAttrs
	 */
	public Map<String, Object> getMailAttrs() {
		return mailAttrs;
	}

	/**
	 * @param mailAttrs
	 *            the mailAttrs to set
	 */
	public void setMailAttrs(Map<String, Object> mailAttrs) {
		this.mailAttrs = mailAttrs;
	}

	/**
	 * @return the subject
	 */
	public String getSubject() {
		return subject;
	}

	/**
	 * @param subject
	 *            the subject to set
	 */
	public void setSubject(String subject) {
		this.subject = subject;
	}

	/**
	 * @return the content
	 */
	public String getContent() {
		return content;
	}

	/**
	 * @param content
	 *            the content to set
	 */
	public void setContent(String content) {
		this.content = content;
	}

	/**
	 * @return the bcc
	 */
	public String[] getBcc() {
		return bcc;
	}

	/**
	 * @param bcc
	 *            the bcc to set
	 */
	public void setBcc(String[] bcc) {
		this.bcc = bcc;
	}

	/**
	 * @return the cc
	 */
	public String[] getCc() {
		return cc;
	}

	/**
	 * @param cc
	 *            the cc to set
	 */
	public void setCc(String[] cc) {
		this.cc = cc;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String[] getReplyTo() {
		return replyTo;
	}

	public void setReplyTo(String[] replyTo) {
		this.replyTo = replyTo;
	}

	public String[] getTo() {
		return to;
	}

	public void setTo(String[] to) {
		this.to = to;
	}

}
