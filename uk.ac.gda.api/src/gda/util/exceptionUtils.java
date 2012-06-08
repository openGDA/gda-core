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

package gda.util;

import gda.device.corba.CorbaDeviceException;

import org.python.core.PyException;

/**
 * 
 */
public class exceptionUtils {

	/**
	 * Returns an appropriate message from the supplied throwable, with special
	 * handling of {@link CorbaDeviceException}s 
	 * 
	 * @param e the throwable
	 * @return the message
	 * 
	 * This method should only be used to create a string representation of the exception for display to the user
	 */
	public static String getMessage(Throwable e) {
		if (e instanceof gda.device.corba.CorbaDeviceException) {
			return ((gda.device.corba.CorbaDeviceException) e).message;
		} 
		return e.toString();
	}

	/**
	 * Returns a string containing the message of the supplied throwable plus
	 * the messages of its nested causes. For {@link PyException}s, also
	 * includes the stack trace of the throwable.
	 * 
	 * @param e the throwable
	 * @return full stack message
	 * 
	 * This method should only be used to create a string representation of the exception for display to the user
	 */
	public static String getFullStackMsg(Throwable e) {
		String msg = getMessage(e);
		Throwable cause = e.getCause();
		while (cause != null && cause != cause.getCause()) {
			msg += "\n" + getMessage(cause);
			cause = cause.getCause();
		}
		if(e instanceof PyException){
			StackTraceElement [] trace = e.getStackTrace();
			for ( StackTraceElement t : trace){
				msg += "\n" +   t.toString();
			}
		}
		return msg;
	}

	/**
	 * Uses a logger to log a throwable as an error. Uses
	 * {@code getFullStackMsg} to construct a message for the log entry.
	 * 
	 * @param logger the logger
	 * @param e the throwable to log
	 */
	public static void logException(org.slf4j.Logger logger, Throwable e) {
		logger.error(getFullStackMsg(e),e);
	}

	/**
	 * Uses a logger to log a throwable as an error. Appends the output from
	 * {@code getFullStackMsg} to the supplied message.
	 * 
	 * @param logger the logger
	 * @param msg a message
	 * @param e the throwable to log
	 */
	public static void logException(org.slf4j.Logger logger, String msg, Throwable e) {
		logger.error(msg + " " + getFullStackMsg(e),e);
	}
}
