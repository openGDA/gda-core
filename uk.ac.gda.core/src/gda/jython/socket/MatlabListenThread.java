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

package gda.jython.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listens to a port connected to the GDA.
 * <p>
 * To run this class in Matlab, you need to import all the jar files used by the GDA. The client machine must also have
 * Jython installed and the jython.jar in the Jython install directory added to the Matlab classpath as well.
 * <P>
 * To make Matlab talk to the GDA run the following commands:
 * <p>
 * import gda.jython.*
 * <p>
 * import gda.jython.socket.*
 * <p>
 * client=SocketClient()
 * <p>
 * client.setHost('machine running GDA object server')
 * <p>
 * client.setPort(port number);
 * <p>
 * client.connect()
 * <p>
 * newThread1 = MatlabListenThread(client.in)
 * <p>
 * newThread1.start()
 * <p>
 * client.send('print "Hello, World!"')
 * <p>
 * all output which would normally go the GDA terminal will aslo now go to the Matlab standard out.
 * <P>
 * Output from scans will be placed into 2-dimensional string arrays using the readData() method. Users may then display
 * or manipulate that data as they want.
 * <P>
 * Note that the buffer holding scan output must be manually cleared using the clear() method before more scans are run,
 * else data sets will be merged.
 */
public class MatlabListenThread extends Thread {
	private static final Logger logger = LoggerFactory.getLogger(MatlabListenThread.class);

	BufferedReader in = null;

	String output = "";

	Vector<String[]> dataBuffer = new Vector<String[]>();

	/**
	 * MatlabListenThread
	 * 
	 * @param in
	 *            from a SocketClient object which the local process has used to open up the socket connection.
	 */
	public MatlabListenThread(BufferedReader in) {
		this.in = in;
	}

	@Override
	public void run() {
		String fromServer = "";
		try {
			while ((fromServer = in.readLine()) != null) {
				// if line is something like: Writing data to file:167.dat
				// then we know there is a new scan so wipe the data buffer
				if (fromServer.startsWith("Writing data to")) {
					this.clear();
				}

				// check if the line has tabs in
				if (fromServer.lastIndexOf("\t") > -1) {
					logger.debug(fromServer);
					// split information by tabs and save data
					String[] temp = fromServer.split("\t");
					dataBuffer.add(temp);
				} else {
					// else simply print out
					logger.debug(fromServer);
					output += (fromServer);
				}
			}
		} catch (IOException ex) {
			logger.error("Error while communicating with CommandServer via socket: " + ex.getMessage());
		}
	}

	/**
	 * Returns the scan data buffer. This will be all scan data since the last time the clear() method was called.
	 * 
	 * @return String[][]
	 */
	public String[][] readData() {
		// convert vector into an array of string arrays
		String[][] output = new String[dataBuffer.size()][];
		for (int i = 0; i < dataBuffer.size(); i++) {
			output[i] = dataBuffer.get(i);
		}
		return output;
	}

	/**
	 * Clears the scan data buffer
	 */
	public void clear() {
		output = "";
		dataBuffer.clear();
	}
}
