/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.device.xspress;

import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to represent replies from ExafsServer.
 */
public class ExafsServerReply {
	private static final Logger logger = LoggerFactory.getLogger(ExafsServerReply.class);

	private String reply = null;

	private StringTokenizer strtok;

	private String system = null;

	private String process = null;

	private int command = 0;

	private int status = -1;

	private String theRest = null;

	/*
	 * Constructor - reply will be the actual string returned by ExafsServer.
	 */
	/**
	 * @param reply
	 */
	public ExafsServerReply(String reply) {
		// Replies of "" can occur e.g. when waiting for something
		// which actually takes a long time there may be timeouts.
		// Easier to allow null replies to get this far than to
		// deal with it in the server.
		if (reply.length() > 0) {
			this.reply = reply;
			strtok = new StringTokenizer(reply, " \r\n");
			parse();
		} else {
			logger.debug("ExafsServerReply given zero length string");
		}
	}

	/**
	 * Splits the reply string into its various parts.
	 */
	private void parse() {
		system = strtok.nextToken();
		process = strtok.nextToken();
		command = Integer.valueOf(strtok.nextToken()).intValue();
		status = Integer.valueOf(strtok.nextToken()).intValue();

		// The rest of the reply is kept as a single string.
		if (strtok.hasMoreTokens()) {
			theRest = strtok.nextToken("\r\n");
		}
	}

	/**
	 * @return the reply
	 */
	public String getReply() {
		return reply;
	}

	/**
	 * @return the system
	 */
	public String getSystem() {
		return system;
	}

	/**
	 * @return the process
	 */
	public String getProcess() {
		return process;
	}

	/**
	 * @return the command
	 */
	public int getCommand() {
		return command;
	}

	/**
	 * @return the status
	 */
	public int getStatus() {
		return status;
	}

	/**
	 * @return the rest
	 */
	public String getRest() {
		return theRest;
	}

	@Override
	public String toString() {
		return reply;
	}
}
