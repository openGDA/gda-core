/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package gda.messages;

import java.util.List;

import gda.jython.UserMessage;

public interface MessageHandler {
	
	/**
	 * Save a message - e.g. to disk.
	 */
	public void saveMessage(String visit, UserMessage message);
	
	/**
	 * Get previous messages for a visit.
	 */
	public List<UserMessage> getMessageHistory(String visit);

}
