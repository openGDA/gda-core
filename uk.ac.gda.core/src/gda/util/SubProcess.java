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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SubProcess Class
 */
public class SubProcess {
	private static final Logger logger = LoggerFactory.getLogger(SubProcess.class);

	private Process process = null;

	/**
	 * @param command
	 * @param synchronous
	 */
	public SubProcess(String command, boolean synchronous) {
		this(command, null, null, synchronous);
	}

	/**
	 * @param command
	 * @param envp
	 * @param dir
	 * @param synchronous
	 */
	public SubProcess(String command, String[] envp, File dir, boolean synchronous) {
		try {
			process = Runtime.getRuntime().exec(command, envp, dir);
			if (process != null) {
				InputStream error = process.getErrorStream();
				InputStream output = process.getInputStream();
				Thread stdout = uk.ac.gda.util.ThreadManager.getThread(new OutputReader(output), getClass().getName());
				Thread stderr = uk.ac.gda.util.ThreadManager.getThread(new OutputReader(error), getClass().getName());
				stdout.start();
				stderr.start();
				if (synchronous)
					process.waitFor();
			}
		} catch (Exception e) {
			logger.error("Error running sub-process " + command);
			logger.error(e.getMessage());
		}
	}

	/**
	 * @return process
	 */
	public Process getProcess() {
		return process;
	}

	static class OutputReader implements Runnable {
		InputStream is;

		/**
		 * @param is
		 */
		public OutputReader(InputStream is) {
			this.is = is;
		}

		@Override
		public void run() {
			String temp = null;

			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(is));
				while ((temp = in.readLine()) != null) {
					logger.debug(temp);
				}
				is.close();
			} catch (Exception e) {
				logger.debug(e.getStackTrace().toString());
			}
		}
	}

	/**
	 * 
	 */
	public void destroy() {
		process.destroy();
	}
}
