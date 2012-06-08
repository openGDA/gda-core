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
import java.util.List;


/**
 *
 */
public class InvalidBeanException extends Exception {

	private List<InvalidBeanMessage> messages;
	
    public InvalidBeanException(final String message) {
    	super(message);
    	setMessages(new ArrayList<InvalidBeanMessage>(1));
    	messages.add(new InvalidBeanMessage(message));
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
		buf.append("\n\n****** Errors identified in settings ******\n");
		for (InvalidBeanMessage m : messages) {
			buf.append(m);
			buf.append("\n");
		}
		buf.append("*************************************");
		return buf.toString();
	}

}
