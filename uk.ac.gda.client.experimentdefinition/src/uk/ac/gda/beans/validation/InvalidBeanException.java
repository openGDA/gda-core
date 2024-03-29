/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package uk.ac.gda.beans.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class InvalidBeanException extends Exception {

	private List<InvalidBeanMessage> messages;
	private WarningType severity;

    public InvalidBeanException(final String message) {
    	super(message);
    	List<InvalidBeanMessage> messageList = new ArrayList<>();
    	messageList.add(new InvalidBeanMessage(WarningType.HIGH, message));
    	setMessages(messageList);
    }
	/**
	 * @param messages
	 * @param cause
	 */
	public InvalidBeanException(List<InvalidBeanMessage> messages, Throwable cause) {
		super(cause);
		setMessages(messages);
	}

	/**
	 * @param messages
	 */
	public InvalidBeanException(List<InvalidBeanMessage> messages) {
		super();
		setMessages(messages);
	}

	/**
	 *
	 * @param messages
	 */
	public void setMessages(List<InvalidBeanMessage> messages) {
		this.messages = messages;

		// Set the severity level to match the highest level in the message list
		// (default to HIGH if no severity levels are present)
		messages
			.stream()
			.max(Comparator.comparing(InvalidBeanMessage::getSeverity))
			.ifPresentOrElse(m -> setSeverity(m.getSeverity()), () -> setSeverity(WarningType.HIGH));
	}

	/**
	 * @return Returns the messages.
	 */
	public List<InvalidBeanMessage> getMessages() {
		return messages;
	}

	@Override
	public String getMessage() {
		final StringBuilder buf = new StringBuilder();
		String heading = switch (getSeverity()) {
			case HIGH -> "\n****** Error identified in XML ******\n";
			default -> "\n****** Warning identified in XML ******\n";
		};
		buf.append(heading);

		// Sort the messages into order of severity so highest will be displayed first
		Collections.sort(messages, (b1, b2) -> b2.getSeverity().ordinal()-b1.getSeverity().ordinal());

		for (InvalidBeanMessage m : messages) {
			buf.append(m);
			buf.append("\n");
		}
		buf.append("*************************************");
		return buf.toString();
	}

	/**
	 * @param severity
	 */
	public void setSeverity(WarningType severity) {
		this.severity = severity;
	}

	/**
	 * If severity has not been set then this will set it to HIGH
	 * @return Returns the severity of the exception
	 */
	public WarningType getSeverity() {
		if(severity==null) {
			severity=WarningType.HIGH;
		}
		return severity;
	}
}