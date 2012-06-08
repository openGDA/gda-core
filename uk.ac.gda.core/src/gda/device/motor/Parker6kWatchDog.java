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

package gda.device.motor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parker6kWatchDog class for Parker 6k motor controller. This sends a heartbeat packet to watchdog port 5004 of the
 * controller that specifies a timeout period and number of retries. This runs every half timeout period keeping the 6k
 * watchdog alive. If the 6k does not receive the heartbeat e.g. due to network errors or the Java program has
 * terminated then the 6k times out and resets the TCP servers on ports 5002 and 5004 ready for new connections. The
 * motor Java code has no "tidyup()" method and Java will not reliably call a finalize method so this is the only way to
 * (badly) disconnect from the 6k. To use this class, construct with host name or IP address string and timeout value in
 * secs then call setup() method to connect to the watchdog port and launch the daemon thread (which can
 * auto-reconnect).
 */
class Parker6kWatchDog implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(Parker6kWatchDog.class);

	private String host = null; // 6k host name or IP address

	private final int portNo = 5004; // TCP/IP watchdog port number

	private Socket parkerSocket = null; // input/output raw 6k socket

	private BufferedWriter out = null; // buffered socket outputwrapper

	private BufferedReader in = null; // buffered socket input wrapper

	private Thread watchDogThread; // thread object for this object

	// 6k heartbeat message 2 bytes timeout = 10 secs default,
	// 2 bytes = 2 retries fixed and 8 bytes spare
	private char timeoutMsg[] = { 0, 10, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0 };

	private char reply[] = new char[12]; // holds reply heartbeat echo

	/**
	 * Must only accept constructor with host name and required timeout period in seconds.
	 */
	@SuppressWarnings("unused")
	private Parker6kWatchDog() {
	}

	/**
	 * @param host
	 * @param timeout
	 */
	public Parker6kWatchDog(String host, int timeout) {
		final int minTimeout = 1, maxTimeout = 255;
		this.host = host;
		// set message field to timeout specified if in 8 bit range secs
		// or otherwise leave at 10 secs default
		if (timeout >= minTimeout && timeout <= maxTimeout) {
			timeoutMsg[1] = (char) timeout;
		}
	}

	/**
	 * set up initial connections to 6k watchdog port and wrap the stream in buffered reader and writer
	 * 
	 * @throws Exception
	 */
	public synchronized void connect() throws Exception {
		parkerSocket = new Socket(host, portNo);
		out = new BufferedWriter(new OutputStreamWriter(parkerSocket.getOutputStream()));
		in = new BufferedReader(new InputStreamReader(parkerSocket.getInputStream()));
	}

	/**
	 * send heartbeat packet to 6k watchdog port
	 * 
	 * @throws Exception
	 */
	public synchronized void sendHeartbeat() throws Exception {
		// send timeout and retries heartbeat to watchdog port
		out.write(timeoutMsg, 0, timeoutMsg.length);
		out.flush();

		// obtain echo reply of heartbeat from watchdog port
		in.read(reply, 0, reply.length);

		logger.debug("Parker6kWatchDog: " + new String(reply));
	}

	/**
	 * setup and start daemon thread to service watchdog port after first attempting to make an initial connection
	 */
	public synchronized void setup() {
		try {
			connect();
		} catch (Exception e) {
		}

		watchDogThread = uk.ac.gda.util.ThreadManager.getThread(this, getClass().getName()); // create
		// thread

		watchDogThread.setDaemon(true); // make thread daemon
		watchDogThread.start(); // start thread now
	}

	/**
	 * tidy up all streams before ending program due to flawed architecture, this can never be called.
	 * 
	 * @throws IOException
	 */
	public synchronized void disconnect() throws IOException {
		out.close();
		in.close();
		parkerSocket.close();
	}

	/**
	 * active thread to send heartbeat and wait for half watchdog port timeout period. If exceptions are thrown, attempt
	 * to reconnect to watchdog port.
	 */
	@Override
	public synchronized void run() {
		// delay between watchdog packet sends (half msec timeout period)
		final long interval = timeoutMsg[1] * 1000 / 2;

		while (true) {
			try {
				sendHeartbeat();
			} catch (Exception e) {
				logger.error("Exception caught in setup (setHeartbeat): " + e.toString());
				// stay in retry loop until reconnected to watchdog port
				while (true) {
					try {
						connect();
						sendHeartbeat();
					} catch (Exception ex) {
						logger.error("Exception caught in setup (connect): " + ex.toString());
						// Thread.yield();
						continue;
					}
					break;
				}
			}
			try {
				// suspend thread for half watchdog timeout period
				wait(interval);
			} catch (Exception ex) {
				logger.error("Exception caught in setup (wait): " + ex.toString());
			}
		}
	}
}
