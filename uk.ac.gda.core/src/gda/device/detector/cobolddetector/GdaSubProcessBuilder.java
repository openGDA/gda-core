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

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.concurrent.Async;

/**
 * Initiate a secondary process to GDA
 */
public class GdaSubProcessBuilder {

	private static final Logger logger = LoggerFactory.getLogger(GdaSubProcessBuilder.class);

	private ArrayList<String> args = new ArrayList<String>();
	private ProcessBuilder pb;

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

		Future<String> command = Async.submit(() -> {
				StringBuilder reply = new StringBuilder();
				String line = "";
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
					reply.append(line);
					logger.info(line);
				}
				p.getInputStream().close();
				br.close();
				return reply.toString();
		});
		if (sync)
			try {
				return command.get(5000, MILLISECONDS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				String msg = "Thread interrupted while waiting for command to complete";
				logger.error(msg, e);
				throw new RuntimeException(msg);
			} catch (ExecutionException e) {
				logger.error("Exception running external command: {}", proc, e.getCause());
			} catch (TimeoutException e) {
				logger.error("External command took too long to complete", e);
			}
		return "";
	}
}
