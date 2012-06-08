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
 * HumiditySocketExecutor Class
 */
public class HumiditySocketExecutor implements SocketExecutor {
	
	private static final Logger logger = LoggerFactory.getLogger(HumiditySocketExecutor.class);
	
	private static final String SEPARATOR = "$";

	private double humidity = 0.25;

	@Override
	public Status execute(String command, BufferedWriter writer) {
		logger.info("HumiditySocketExecutor executing socket command " + command);

		try {
			if (command.startsWith("quit")) {
				writer.close();
			} else {
				if (command.equals("?DAT HUM" + SEPARATOR)) {
					writer.write(Double.toString(humidity) + SEPARATOR + Double.toString(humidity + 0.025) + SEPARATOR);
					humidity += 0.05;
				} else {
					writer.write("Ok");
				}
				writer.newLine();
				writer.flush();
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
