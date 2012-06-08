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
 * A command line toolkit to synchronously put a value to a specified process variable.
 */
public class JCAPut {
	private static final Logger logger = LoggerFactory.getLogger(JCAPut.class);

	private static final double TIMEOUT = 3.0;

	double timeout = TIMEOUT;

	String name = null;

	String value = null;

	boolean pvSpecified = false;

	/**
	 * Default constructor
	 */
	public JCAPut() {
	}

	/**
	 * Main entry point
	 * 
	 * @param args
	 *            arguments
	 */
	public static void main(String[] args) {
		JCAPut jcaput = new JCAPut();
		JCALibrary jca = null;
		Context ctxt = null;
		Channel chanput = null;
		Channel changet = null;

		// Parse the command line
		if (!jcaput.parseCommand(args))
			System.exit(1);
		if (!jcaput.pvSpecified) {
			System.err.println("Missing inputs\n");
			try {
				Thread.sleep(100);
			} catch (InterruptedException e1) {
				logger.debug("InterruptedException: " + e1);
			}

			if (jcaput.name == null || jcaput.name == "") {
				System.err.println("No PV name specified\n");
				logger.debug("Please enter an EPICS PV name:\n");
				try {
					BufferedReader is = new BufferedReader(new InputStreamReader(System.in));
					jcaput.name = is.readLine();
					is.close();
				} catch (IOException e) {
					System.err.println("IOException: " + e);
				}
			}
			if (jcaput.value == null || jcaput.value == "") {
				System.err.println("No PV value specified\n");
				logger.debug("Please enter a PV value:\n");
				try {
					BufferedReader is = new BufferedReader(new InputStreamReader(System.in));
					jcaput.value = is.readLine();
					is.close();
				} catch (IOException e) {
					System.err.println("IOException: " + e);
				}
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
			chanput = ctxt.createChannel(jcaput.name);
			changet = ctxt.createChannel(jcaput.name);

			// Wait for search
			ctxt.pendIO(jcaput.timeout);

			chanput.printInfo();

		} catch (Exception ex) {
			System.err.println("Search failed for " + jcaput.name + ":\n" + ex);
			System.exit(1);
		}

		// if search successful
		if (ctxt != null && chanput != null && changet != null) {

			try {
				// Put the value
				chanput.put(jcaput.value);

				// Wait for the put
				ctxt.pendIO(jcaput.timeout);

				// Print the requested value
				logger.debug("The value of " + jcaput.name + " is set to " + jcaput.value);

			} catch (Exception ex) {
				System.err.println("Put failed for " + jcaput.name + ":\n" + ex);
				System.exit(1);
			}

			// wait loop until completed
			try {
				String[] val;
				do {
					logger.debug("The value of " + jcaput.name + " is to be set at " + jcaput.value);
					val = ((STRING) changet.get(DBRType.STRING, 1)).getStringValue();
					ctxt.pendIO(jcaput.timeout);
				} while (!val[0].equals(jcaput.value));
				logger.debug("The value of " + jcaput.name + " is now at " + val[0]);

			} catch (Exception e) {
				System.err.println("Get failed for " + jcaput.name + ":\n" + e);
			}

			// Clean up
			try {
				// Clear the channel
				chanput.destroy();
				changet.destroy();
				// Destroy the context
				ctxt.destroy();

			} catch (Exception ex) {
				System.err.println("Clean up failed for " + jcaput.name + ":\n" + ex);
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
					System.err.println("\n\nInvalid option: in case 1 " + args[i]);
					usage();
					return false;
				}
			} else {

				if (!pvSpecified) {

					name = args[i];
					int j = i++;
					if (j < args.length) {
						value = args[j];
					}

					if (name != null && value != null) {
						pvSpecified = true;
					}

				} else {
					System.err.println("\n\nInvalid option: in case 2 " + pvSpecified + " " + args[i]);
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
		System.err.println("\nUsage: java JCAPut [Options] pvname value\n"
				+ "  Connects to pvname and sets the value\n" + "\n" + "  Options:\n"
				+ "    -h help      This message\n" + "    -t float     Timeout in seconds (Default: " + TIMEOUT
				+ ")\n");
	}
}