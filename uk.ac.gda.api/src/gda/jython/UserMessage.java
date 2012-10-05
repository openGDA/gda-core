/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.jython;

import java.io.Serializable;
import java.util.Date;

/**
 * Object to be passed from JythonServer to Client when a user broadcasts a message to other users.
 */
public class UserMessage implements Serializable{

	int sourceClientNumber;
	String sourceUsername;
	String message;
	long timestamp;

	/**
	 * @param sourceClientNumber
	 * @param sourceUsername
	 * @param message
	 */
	public UserMessage(int sourceClientNumber, String sourceUsername, String message) {
		this(sourceClientNumber, sourceUsername, message, System.currentTimeMillis());
	}

	UserMessage(int sourceClientNumber, String sourceUsername, String message, long timestamp) {
		this.sourceClientNumber = sourceClientNumber;
		this.sourceUsername = sourceUsername;
		this.message = message;
		this.timestamp = timestamp;
	}

	/**
	 * @return Returns the sourceClientNumber.
	 */
	public int getSourceClientNumber() {
		return sourceClientNumber;
	}

	/**
	 * @return Returns the sourceUsername.
	 */
	public String getSourceUsername() {
		return sourceUsername;
	}

	/**
	 * @return Returns the message.
	 */
	public String getMessage() {
		return message;
	}
	
	public Date getTimestamp() {
		return new Date(timestamp);
	}
}
