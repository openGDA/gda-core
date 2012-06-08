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

package gda.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DummySocketExecutor Class
 */
public class DummySocketExecutor implements SocketExecutor {
	
	private static final Logger logger = LoggerFactory.getLogger(DummySocketExecutor.class);
	
	@Override
	public Status execute(String command, BufferedWriter writer) {
		logger.info("DummySocketExecutor executing socket command " + command);

		try {
			writer.write("Ok");
			writer.newLine();
			writer.flush();

			if (command.startsWith("quit")) {
				writer.close();
			}
		} catch (IOException ioex) {
			logger.error("IOException caught in DummySocketExecutor");
		}

		return Status.SUCCESS;
	}

	@Override
	public void setSocket(Socket socket) {
	}
}
