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

package gda.epics.cmdline;

import gov.aps.jca.Channel;
import gov.aps.jca.Context;
import gov.aps.jca.JCALibrary;
import gov.aps.jca.dbr.DBRType;
import gov.aps.jca.dbr.STRING;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A command line toolkit to get a process variable
 */
public class JCAGet {
	private static final Logger logger = LoggerFactory.getLogger(JCAGet.class);

	private static final double TIMEOUT = 1.0;

	double timeout = TIMEOUT;

	String name = null;

	String[] value = null;

	boolean pvSpecified = false;

	/**
	 * Default constructor
	 */
	public JCAGet() {
	}

	/**
	 * Main entry point
	 * 
	 * @param args
	 *            arguments
	 */
	public static void main(String[] args) {
		JCAGet jcaget = new JCAGet();
		JCALibrary jca = null;
		Context ctxt = null;
		Channel chan = null;

		// Parse the command line
		if (!jcaget.parseCommand(args))
			System.exit(1);
		if (!jcaget.pvSpecified) {
			System.err.println("No PV specified\n");
			try {
				Thread.sleep(100);
			} catch (InterruptedException e1) {
				logger.debug("InterruptedException: " + e1);
			}

			logger.debug("Please enter an EPICS PV:\n");
			try {
				BufferedReader is = new BufferedReader(new InputStreamReader(System.in));
				jcaget.name = is.readLine();
				is.close();
			} catch (IOException e) {
				System.err.println("IOException: " + e);
			}
			// System.exit(1);
		}

		// Initialize and search
		try {
			// Get the JCALibrary instance
			jca = JCALibrary.getInstance();

			// Create a single threaded context with default configuration
			// values
			ctxt = jca.createContext(JCALibrary.CHANNEL_ACCESS_JAVA);

			// Search
			chan = ctxt.createChannel(jcaget.name);

			// Wait for search
			ctxt.pendIO(jcaget.timeout);
			// chan.printInfo();

		} catch (Exception ex) {
			System.err.println("Search failed for " + jcaget.name + "\n" + ex);
			System.exit(1);
		}

		if (ctxt != null && chan != null) {
			// Get the first value as a String
			try {
				// Get the value
				jcaget.value = ((STRING) chan.get(DBRType.STRING, 1)).getStringValue();

				// Wait for the get
				ctxt.pendIO(jcaget.timeout);

				// Print the value
				logger.debug("The value of " + jcaget.name + " is " + jcaget.value[0]);

			} catch (Exception ex) {
				System.err.println("Get failed for " + jcaget.name + "\n" + ex);
				System.exit(1);
			}

			// Clean up
			try {
				// Clear the channel
				chan.destroy();

				// Destroy the context
				ctxt.destroy();

			} catch (Exception ex) {
				System.err.println("Clean up failed for " + jcaget.name + ":\n" + ex);
				System.exit(1);
			}
		}

		System.exit(0);

	}

	/**
	 * Parse the command line
	 * 
	 * @param args
	 * @return success or failure
	 */
	private boolean parseCommand(String[] args) {
		int i;

		for (i = 0; i < args.length; i++) {
			if (args[i].startsWith("-")) {
				switch (args[i].charAt(1)) {
				case 'h':
					usage();
					System.exit(0);
					break;
				case 't':
					try {
						timeout = Double.valueOf(args[++i]).doubleValue();
					} catch (NumberFormatException ex) {
						System.err.println("\n\nInvalid timeout: " + args[i]);
						usage();
						return false;
					}
					break;
				default:
					System.err.println("\n\nInvalid option: " + args[i]);
					usage();
					return false;
				}
			} else {
				if (!pvSpecified) {
					name = args[i];
					pvSpecified = true;
				} else {
					System.err.println("\n\nInvalid option: " + args[i]);
					usage();
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Print usage
	 */
	private void usage() {
		System.err.println("\nUsage: java JCAGet [Options] pvname\n" + "  Connects to pvname and gets the value\n"
				+ "\n" + "  Options:\n" + "    -h help      This message\n"
				+ "    -t float     Timeout in seconds (Default: " + TIMEOUT + ")\n");
	}

}