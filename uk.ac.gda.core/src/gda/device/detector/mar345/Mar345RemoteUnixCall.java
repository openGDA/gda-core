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

package gda.device.detector.mar345;

import gda.configuration.properties.LocalProperties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The mar software runs on a server/workstation connected directly to the mar detector and the GDA client and server
 * will be running on different machines. The MAR will be operated by using the scan345 command which reads commands
 * from mar.com and writes messages to mar.message so we need to be able to start and the scan345 command on the mar
 * server. We don't have to worry about writing and reading mar.com and mar.message because the mar directory is nfs
 * mounted onto the GDA server or client machine. (At Diamond the MAR software is located in /dls_sw/apps/i15/mar) To
 * operate the scan345 we will run bash scripts which will remotely execute or stop scan345 via ssh. 2
 */

public class Mar345RemoteUnixCall {

	private static final Logger logger = LoggerFactory.getLogger(Mar345RemoteUnixCall.class);

	private static Runtime runtime = Runtime.getRuntime();

	private static void start(String command) throws Exception {
		if (command != null) {
			runtime = Runtime.getRuntime();
			logger.debug("Starting " + command);
			runtime.exec(command);
		}
	}

	private static void stop(String commandIdentifier) {
		if (commandIdentifier != null) {
			String progarray[] = {
					"/bin/csh",
					"-c",
					"kill -TERM `ps -efl | grep \"" + commandIdentifier + "\" | " + "grep -v grep |  "
							+ "awk '{print $4}'`" };

			try {
				logger.debug("Stoping via identifier => " + commandIdentifier);
				(runtime.exec(progarray)).waitFor();
			} catch (Exception ex) {
				logger.debug("Error stopping process '{}'", commandIdentifier, ex);
			}
		}
	}

	/**
	 * Start the sub-process to control the Mar345 detector. This process runs as a daemon and reads commands from a
	 * file which is written by the Mar345Detector class.
	 */
	public static void start345Daemon() {
		try {
			start(LocalProperties.get("gda.device.detector.mar345.scan345Command"));
			runtime.addShutdownHook(uk.ac.gda.util.ThreadManager.getThread(new Runnable() {
				@Override
				public void run() {
					stop345Daemon();
				}
			}));
		} catch (Exception ex) {
			logger.debug("Error starting daemon", ex);
		}
	}

	/**
	 * Stops the sub-process to control the Mar345 detector. This process runs as a daemon and reads commands from a
	 * file which is written by the Mar345Detector class.
	 */
	public static void stop345Daemon() {
		try {
			stop(LocalProperties.get("gda.device.detector.mar345.scan345StopString"));
		} catch (Exception ex) {
			logger.debug("Error stopping daemon", ex);
		}
	}

}
