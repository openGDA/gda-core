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

import gda.epics.util.JCAUtils;
import gov.aps.jca.CAStatus;
import gov.aps.jca.Channel;
import gov.aps.jca.Context;
import gov.aps.jca.JCALibrary;
import gov.aps.jca.Monitor;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBRType;
import gov.aps.jca.dbr.STRING;
import gov.aps.jca.event.ConnectionEvent;
import gov.aps.jca.event.ConnectionListener;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;
import gov.aps.jca.event.PutEvent;
import gov.aps.jca.event.PutListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A command line toolkit to get a process variable
 */
public class AsynPut {
	private static final Logger logger = LoggerFactory.getLogger(AsynPut.class);

	private static final double TIMEOUT = 1.0;

	private double timeout = TIMEOUT;

	private String name = null;

	private String value = null;

	boolean pvSpecified = false;

	private int connectionCounter = 0;

	private boolean nameFound = false;

	private boolean putCallbackDone = false;

	/**
	 * Default constructor
	 */
	public AsynPut() {
	}

	/**
	 * Implementation of Connection Listener class
	 */
	private class JCAConnectionListener implements ConnectionListener {
		@Override
		public void connectionChanged(ConnectionEvent ev) {
			onConnectionChanged(ev);
		}
	}

	/**
	 * Implementation of MonitorListener class
	 */
	private class JCAMonitorListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent ev) {
			onValueChanged(ev);
		}
	}

	/**
	 * Implementation of PutListener class
	 */
	private class JCAPutListener implements PutListener {
		@Override
		public void putCompleted(PutEvent ev) {
			onPutCompleted(ev);
		}
	}

	/**
	 * Main entry point
	 * 
	 * @param args
	 *            arguments
	 */
	public static void main(String[] args) {
		AsynPut jcaput = new AsynPut();
		JCALibrary jca = null;
		Context ctxt = null;
		Channel chan = null;

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

		// Initialize
		try {
			// Get the JCALibrary instance
			jca = JCALibrary.getInstance();

			// Create a single threaded context with default configuration
			// values
			ctxt = jca.createContext(JCALibrary.CHANNEL_ACCESS_JAVA);
			// ctxt.printInfo();

		} catch (Exception ex) {
			System.err.println("Initialization failed for " + jcaput.name + ":\n" + ex);
			System.exit(1);
		}

		// Search
		try {
			if (ctxt != null) {
				chan = ctxt.createChannel(jcaput.name, jcaput.new JCAConnectionListener());
				ctxt.pendIO(jcaput.timeout);
			}
		} catch (Exception ex) {
			System.err.println("Search failed for " + jcaput.name + ":\n" + ex);
			System.exit(1);
		}
		logger.debug("Waiting for Connection callback ...");
		while (!jcaput.nameFound) {
			// System.out.print(".");
		}

		// Put the value to the channel
		try {
			if (ctxt != null && chan != null) {
				chan.put(jcaput.value, jcaput.new JCAPutListener());
				ctxt.pendIO(jcaput.timeout);
			}
		} catch (Exception ex) {
			System.err.println("Put failed for " + jcaput.name + ":\n" + ex);
			System.exit(1);
		}

		logger.debug("The value of " + jcaput.name + " is set to " + jcaput.value);
		// wait for call back
		logger.debug("Waiting for Put Callback ...");
		while (!jcaput.putCallbackDone) {
			// System.out.print(".");
		}
		logger.debug("Put is done.");

		// Clean up
		try {
			if (chan != null) {
				// Clear the channel
				chan.destroy();
			}

			if (ctxt != null) {
				// Destroy the context
				ctxt.destroy();
			}
		} catch (Exception ex) {
			System.err.println("Clean up failed for " + jcaput.name + ":\n" + ex);
			System.exit(1);
		}
		System.exit(0);

	}

	// Callbacks
	// //////////////////////////////////////////////////////////////////

	private void onPutCompleted(PutEvent ev) {
		Channel ch = (Channel) ev.getSource();
		Context ctxt = ch.getContext();
		// int count = ev.getCount();
		// CAStatus st = ev.getStatus();
		// DBRType type = ev.getType();
		try {
			String[] value = ((STRING) ch.get(DBRType.STRING, 1)).getStringValue();
			ctxt.pendIO(timeout);
			logger.debug("The value of " + this.name + " is now at " + value[0]);
		} catch (Exception e) {
			System.err.println("Get failed for " + this.name + ":\n" + e);
		}
		putCallbackDone = true;
	}

	/**
	 * Connection callback
	 * 
	 * @param ev
	 */
	private void onConnectionChanged(ConnectionEvent ev) {
		Channel ch = (Channel) ev.getSource();
		Context ctxt = ch.getContext();
		// Start a monitor on the first connection
		if (connectionCounter == 0 && ch.getConnectionState() == Channel.CONNECTED) {
			// This is the first connection.
			try {
				// Print some information
				nameFound = true;
				logger.debug(JCAUtils.timeStamp() + " Search successful for: " + getName());
				// ch.printInfo();

				// Add a monitor listener
				ch.addMonitor(DBRType.STRING, 1, Monitor.VALUE | Monitor.LOG | Monitor.ALARM, new JCAMonitorListener());
				ctxt.pendIO(timeout);
			} catch (Exception ex) {
				System.err.println("Add Monitor failed for " + getName() + ":\n" + ex);
				return;
			}
		}

		// Print connection state
		System.out.print(JCAUtils.timeStamp() + " ");
		if (ch.getConnectionState() == Channel.CONNECTED) {
			logger.debug(ch.getName() + " is connected");
			connectionCounter++;
		} else if (ch.getConnectionState() == Channel.CLOSED) {
			logger.debug(ch.getName() + " is closed");
		} else if (ch.getConnectionState() == Channel.DISCONNECTED) {
			logger.debug(ch.getName() + " is disconnected");
		} else if (ch.getConnectionState() == Channel.NEVER_CONNECTED) {
			logger.debug(ch.getName() + " is never connected");
		} else {
			// Shouldn't happen
			logger.debug(ch.getName() + " is in an unknown state");
		}
	}

	/**
	 * Monitor callback
	 * 
	 * @param ev
	 */
	private void onValueChanged(MonitorEvent ev) {
		Channel ch = (Channel) ev.getSource();
		// Check the status
		if (ev.getStatus() != CAStatus.NORMAL) {
			System.err.println("monitorChanged: Bad status for: " + getName());
		}
		// Get the value from the DBR
		try {
			DBR dbr = ev.getDBR();
			String[] value = ((STRING) dbr).getStringValue();
			System.out.print(JCAUtils.timeStamp() + " " + getName() + " :: ");
			DBR.printValue(value);
		} catch (Exception ex) {
			System.err.println("monitorChanged: Bad value for " + ch.getName() + ":\n " + ex);
			return;
		}
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
					int j = i + 1;
					if (j < args.length) {
						value = args[j];
					}
					if (name != null && value != null) {
						pvSpecified = true;
					}
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
		System.err.println("\nUsage: java JCAPut [Options] pvname value\n"
				+ "  Connects to pvname and sets the value\n" + "\n" + "  Options:\n"
				+ "    -h help      This message\n" + "    -t float     Timeout in seconds (Default: " + TIMEOUT
				+ ")\n");
	}

	// Accessors
	// //////////////////////////////////////////////////////////////////
	/**
	 * Set process variable name
	 * 
	 * @param name
	 *            process variable name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Get process variable name
	 * 
	 * @return process variable name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set timeout
	 * 
	 * @param timeout
	 *            timeout in sec
	 */
	public void setTimeout(double timeout) {
		this.timeout = timeout;
	}

	/**
	 * Get timeout
	 * 
	 * @return timeout in sec
	 */
	public double getTimeout() {
		return timeout;
	}

	/**
	 * @return if PV name has been found
	 */
	public boolean isNameFound() {
		return nameFound;
	}

}