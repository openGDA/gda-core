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

package gda.device.detector;

import gda.device.Detector;
import gda.device.DeviceException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Class to communicate with the SESO XBPM software to get the beam position.
 */
public class SesoXbpmDetector extends DetectorBase implements Detector {
	double[] xy = new double[2];

	// Default values. These should be defined in the configuration.
	String host = "localhost";

	int port = 20;

	@Override
	public void collectData() throws DeviceException {
		try {
			Socket sock = new Socket(host, port);
			PrintWriter out = new PrintWriter(sock.getOutputStream(), true);
			out.print("?");
			out.flush();
			BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));

			String strBuffer = in.readLine();
			String[] strPosition;
			// The SESO XBPM software returns the x,y position sometimes
			// delimited
			// by a comma and sometimes by a space! So we need to check for
			// both.
			if (strBuffer.contains(",")) {
				strPosition = strBuffer.split(",");
			} else {
				strPosition = strBuffer.split("  ");
			}
			xy[0] = Double.parseDouble(strPosition[0]);
			xy[1] = Double.parseDouble(strPosition[1]);

			// Close the socket.
			sock.close();
		} catch (UnknownHostException e) {
			throw new DeviceException("SesoXbpmDetector: Cannot connect to host.", e);
		} catch (IOException e) {
			throw new DeviceException("SesoXbpmDetector: Error reading from detector.", e);
		}
	}

	@Override
	public int getStatus() throws DeviceException {
		return 0;
	}

	@Override
	public Object readout() throws DeviceException {
		return xy;
	}

	/**
	 * Set the host name of the Windows PC running the SESO Software. Used by castor for instantiation.
	 * 
	 * @param host
	 *            the IP host name of the controller
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * Returns the host name of the Windows PC running ImagePro.
	 * 
	 * @return the host name.
	 */
	public String getHost() {
		return host;
	}

	/**
	 * Set the socket number for ethernet communications.
	 * 
	 * @param port
	 *            the socket number.
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * Get the socket number for ethernet communications.
	 * 
	 * @return the port number.
	 */
	public int getPort() {
		return port;
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		// readout() doesn't return a filename.
		return false;
	}

	@Override
	public String getDescription() throws DeviceException {
		return "SESO Beam Position Monitor";
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return "unknown";
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return "XBPM";
	}

}
