/*-
 * Copyright © 2019 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package gda.util;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Future;

import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

import gda.configuration.properties.LocalProperties;
import uk.ac.diamond.daq.concurrent.Async;

/**
 *  Utility builder for sending emails.
 *
 *  Messages can be built and sent with a fluent api, eg
 *  <pre>
 *  new Email().to("foobar@diamond.ac.uk")
 *  		.subject("This is a test message")
 *  		.message("This is the message body")
 *  		.attach("/path/to/file")
 *  		.attach("/path/to/additional_file")
 *  		.send();
 *  </pre>
 */
public class Email {
	private static final Logger logger = LoggerFactory.getLogger(Email.class);

	private Collection<String> to = new ArrayList<>();
	private Collection<String> cc = new ArrayList<>();
	private Collection<String> bcc = new ArrayList<>();
	private String from;
	private String subject;
	private String message;
	private Collection<String> attachments = new ArrayList<>();

	/** Add addresses to be the main recipients of this message */
	public Email to(Collection<String> address) {
		to.addAll(requireNonNull(address, "Recipient list must not be null"));
		return this;
	}

	/** Add addresses to be the main recipients of this message */
	public Email to(String... address) {
		return to(asList(address));
	}

	/** Add addresses to be included in the CC list of this message */
	public Email cc(Collection<String> cc) {
		this.cc.addAll(requireNonNull(cc, "CC list must not be null"));
		return this;
	}

	/** Add addresses to be included in the CC list of this message */
	public Email cc(String... cc) {
		return cc(asList(cc));
	}

	/** Add addresses to be included in the BCC list of this message */
	public Email bcc(Collection<String> bcc) {
		this.bcc.addAll(requireNonNull(bcc, "BCC list must not be null"));
		return this;
	}

	/** Add addresses to be included in the BCC list of this message */
	public Email bcc(String... bcc) {
		return bcc(asList(bcc));
	}

	/** Set the subject of this message */
	public Email subject(String subject) {
		this.subject = subject;
		return this;
	}

	/** Set the body text of this message */
	public Email message(String message) {
		this.message = message;
		return this;
	}

	/** Add an attachment to this message */
	public Email attach(Collection<String> attachments) {
		this.attachments.addAll(requireNonNull(attachments, "Attachments must not be null"));
		return this;
	}

	/** Add an attachment to this message */
	public Email attach(String... attachments) {
		return attach(asList(attachments));
	}

	/** Set the address that this message will appear to be from */
	public Email from(String name, String address) {
		from = formatFrom(name, address);
		return this;
	}

	/** Check that required fields have been set and use defaults for optional ones */
	private void validate() {
		if (from == null) {
			String beamlineName = "GDA " + LocalProperties.get("gda.beamline.name", "??");
			String user = System.getenv("USER");
			String host = System.getenv("HOST");
			from = formatFrom(beamlineName, user + "@" + host);
		}
		if (to.size() + cc.size() + bcc.size() == 0) {
			throw new IllegalStateException("This email has no recipients");
		}
		if (subject == null) {
			subject = "GDA Email";
		}
		if (message == null) {
			message = "";
		}
	}

	/** Add attachment, catching any exceptions */
	private void addAttachment(final MimeMessageHelper helper, FileSystemResource file) {
		try {
			helper.addAttachment(file.getFilename(), file);
		} catch (Exception e) {
			logger.error("Could not add {} as an attachment", file.getFilename(), e);
		}
	}

	/** Send message
	 * @throws MessagingException */
	public void send() throws MessagingException {
		validate();
		final String smtpHost = LocalProperties.get("gda.feedback.smtp.host", "localhost");

		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		mailSender.setHost(smtpHost);

		MimeMessage mimeMessage = mailSender.createMimeMessage();
		final MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, !attachments.isEmpty());
		helper.setFrom(from);
		helper.setTo(to.toArray(new String[] {}));
		helper.setCc(cc.toArray(new String[] {}));
		helper.setBcc(bcc.toArray(new String[] {}));
		helper.setSubject(subject);
		helper.setText(message);

		attachments.stream()
				.map(File::new)
				.map(FileSystemResource::new)
				.forEach(f -> addAttachment(helper, f));

		MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
		mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
		mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
		CommandMap.setDefaultCommandMap(mc);
		mailSender.send(mimeMessage);
	}

	private String formatFrom(final String beamlineName, String address) {
		return String.format("%s <%s>", beamlineName, address);
	}

	/**
	 * Send message in a background thread
	 *
	 * @return a future giving access to the completion status
	 */
	public Future<?> sendAsync() {
		return Async.submit(() -> {
			try {
				send();
			} catch (MessagingException e) {
				logger.error("Could not send email");
				throw new RuntimeException("Couldn't send email", e);
			}
		});
	}
}
