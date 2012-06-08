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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Monitors a process variable
 */
public class JCAMonitor {
	private static final Logger logger = LoggerFactory.getLogger(JCAMonitor.class);

	private static final double TIMEOUT = 10.0;

	private double timeout = TIMEOUT;

	private String name = null;

	private boolean pvSpecified = false;

	private int connectionCounter = 0;

//	private int monitorCounter = 0;

	private boolean nameFound = false;

	/**
	 * Default constructor
	 */
	public JCAMonitor() {
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
	 * Main entry point
	 * 
	 * @param args
	 *            arguments
	 */
	public static void main(String[] args) {
		JCAMonitor jcamonitor = new JCAMonitor();
		JCALibrary jca = null;
		Context ctxt = null;
		Channel chan = null;

		logger.debug(JCAUtils.timeStamp() + " Starting JCA Monitor");

		// Parse the command line
		if (!jcamonitor.parseCommand(args))
			System.exit(1);
		if (!jcamonitor.pvSpecified) {
			System.err.println("No PV specified\n");
			System.exit(1);
		}

		// Initialize JCA
		try {
			// Get the JCALibrary instance
			jca = JCALibrary.getInstance();

			// Create a thread safe context with default configuration
			// values
			ctxt = jca.createContext(JCALibrary.CHANNEL_ACCESS_JAVA);
		} catch (Exception ex) {
			System.err.println("Initialization failed for " + jcamonitor.name + ":\n" + ex);
			System.exit(1);
		}

		// if initialisation successful
		if (ctxt != null) {
			// Search
			try {
				// Search
				chan = ctxt.createChannel(jcamonitor.name, jcamonitor.new JCAConnectionListener());
				ctxt.pendIO(jcamonitor.timeout);
			} catch (Exception ex) {
				System.err.println("Search failed for " + jcamonitor.name + ":\n" + ex);
				System.exit(1);
			}

			// if search successful
			if (chan != null) {
				// Main loop - set how long to monitor
				try {
					long timeoutms = (long) (1000.0 * jcamonitor.getTimeout());
					Thread.sleep(timeoutms);
				} catch (Exception ex) {
					System.err.println(ex);
				}
				if (!jcamonitor.isNameFound()) {
					logger.debug(jcamonitor.getName() + " not found");
				}

				// Clean up
				try {
					// Clear the channel
					chan.destroy();

					// Destroy the context
					ctxt.destroy();
				} catch (Exception ex) {
					System.err.println("Clean up failed for " + jcamonitor.name + ":\n" + ex);
					System.exit(1);
				}
			}
		}
		// Exit
		logger.debug(JCAUtils.timeStamp() + " All Done");
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
		System.err.println("\nUsage: java JCAMonitor [Options] pvname\n" + "  Connects to pvname and gets the value\n"
				+ "\n" + "  Options:\n" + "    -h help      This message\n"
				+ "    -t float     Timeout in seconds (Default: " + TIMEOUT + ")\n");
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

	// Callbacks
	// //////////////////////////////////////////////////////////////////

	/**
	 * Connection callback
	 * 
	 * @param ev
	 */
	private void onConnectionChanged(ConnectionEvent ev) {
		Channel ch = (Channel) ev.getSource();
		Context ctxt = ch.getContext();
		/*
		 * Message.debug(JCAUtils.timeStamp() + " ConnectionEvent for: \n " + ch.getName() + " [" + getName() + "]");
		 */
		// Start a monitor on the first connection
		if (connectionCounter == 0 && ch.getConnectionState() == Channel.CONNECTED) {
			// This is the first connection.
			try {
				// Print some information
				nameFound = true;
				logger.debug(JCAUtils.timeStamp() + " Search successful for: " + getName());
				ch.printInfo();

				// Add a monitor listener
				ch.addMonitor(DBRType.STRING, 1, Monitor.VALUE | Monitor.LOG | Monitor.ALARM, new JCAMonitorListener());
				ctxt.pendIO(timeout);
			} catch (Exception ex) {
				System.err.println("Add Monitor failed for " + getName() + ":\n" + ex);
				System.exit(1);
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
	} // End of onConnectionChanged()

	/**
	 * Monitor callback
	 * 
	 * @param ev
	 */
	private void onValueChanged(MonitorEvent ev) {
		Channel ch = (Channel) ev.getSource();

//		monitorCounter++;
		/*
		 * Message.debug(JCAUtils.timeStamp() + " MonitorEvent for: \n " + ch.getName() + " [" + getName() + "]");
		 */

		// Check the status
		if (ev.getStatus() != CAStatus.NORMAL) {
			logger.debug("monitorChanged: Bad status for: " + getName());
		}

		// Get the value from the DBR
		try {
			DBR dbr = ev.getDBR();
			String[] value = ((STRING) dbr).getStringValue();
			logger.debug(JCAUtils.timeStamp() + " " + getName() + ": ");
			DBR.printValue(value);

			/*
			 * Message.debug(" Information for " + ch.getName() + ":"); dbr.printInfo();
			 */
		} catch (Exception ex) {
			logger.debug("monitorChanged: Bad value for " + ch.getName() + ":\n " + ex);
			return;
		}
	}

}