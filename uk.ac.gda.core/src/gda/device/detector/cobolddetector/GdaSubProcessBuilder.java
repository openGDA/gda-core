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

package gda.device.detector.cobolddetector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Initiate a secondary process to GDA
 */
public class GdaSubProcessBuilder {

	private static final Logger logger = LoggerFactory.getLogger(GdaSubProcessBuilder.class);

	private ArrayList<String> args = new ArrayList<String>();
	private ProcessBuilder pb;
	private String reply = "";

	/**
	 *
	 */
	public GdaSubProcessBuilder() {
		pb = new ProcessBuilder();
		pb.redirectErrorStream(true);
	}

	/**
	 * Run the command
	 *
	 * @param proc
	 * @param sync
	 * @param options
	 * @return the reply
	 * @throws RuntimeException
	 */
	public synchronized String runCommand(String proc, final boolean sync, String... options) throws RuntimeException {
		logger.info("GdaSubProcessBuilder spawning process " + proc);
		args.add(proc);
		for (int i = 0; i < options.length; i++)
			args.add(options[i]);

		pb.command(args);

		Thread processThread = uk.ac.gda.util.ThreadManager.getThread(new Runnable() {
			@Override
			public void run() {
				String line = "";
				try {
					Process p = pb.start();

					BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
					Thread.sleep(200);
					p.waitFor();
					if (br.ready())
						logger.info("GdaSubProcessBuilder: " + args.get(0) + ", reply = ");
					else
						logger.info("GdaSubProcessBuilder: no reply from " + args.get(0));

					int i = 0;
					while (br.ready() && i++ < 20) {
						line = br.readLine();
						reply = reply.concat(line);
						logger.info(line);
					}
					p.getInputStream().close();
					br.close();
				} catch (IOException e) {
					throw new RuntimeException("IOException caught in GdaSubProcessBuilder.processThread: "
							+ e.getMessage());
				} catch (Exception e) {
					throw new RuntimeException("Exception caught in GdaSubProcessBuilder.processThread: "
							+ e.getMessage());
				}
			}
		});

		processThread.start();

		int timeout = 5000;
		int wait = 0;
		if (sync)
			try {
				while (processThread.isAlive() && wait < timeout) {
					Thread.sleep(300);
					wait += 300;
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				String msg = "Thread interrupted while waiting for command to complete";
				logger.error(msg, e);
				throw new RuntimeException(msg);
			}

		if (reply.toLowerCase().contains("exception") || reply.toLowerCase().contains("error")
				|| reply.toLowerCase().contains("overflow")) {
			logger.error(reply);
		}
		return reply;
	}
}
