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

package gda.device.memory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;

/**
 * A memory class for the VME Generic data acquisition histogramming memory card implemented using DA.Server
 */
public class Scaler extends Gdhist {

	private static final Logger logger = LoggerFactory.getLogger(Scaler.class);

	@Override
	protected void open() throws DeviceException {
		String cmd = openCommand;
		handle = -1;

		if (!openCommand.startsWith("scaler") && !openCommand.startsWith("tfg open-cc")) {
			logger.error("Invalid open command for " + getName());
			throw new DeviceException("Scaler: Invalid open command for " + this.getName());
		}
		handle = ((Integer) sendAndParse("open", cmd)).intValue();

		if (startupScript != null) {
			String command = "~" + startupScript + " " + width;
			if (height > 1) {
				command += " " + height;
			}
			sendAndParse("startup script", command);
		}
	}

	@Override
	public float[] readFloat(int x, int y, int t, int dx, int dy, int dt) throws DeviceException {
		float[] data = new float[0];
		int npoints = dx * dy * dt;

		ensureOpen();

		try {
			data = daServer.getFloatBinaryData("read " + t + " " + y + " " + x + " " + dt + " " + dy + " " + dx + " "
					+ localEndian + " float from " + handle, npoints);
		} catch (Exception e) {
			throw new DeviceException(
					String.format("Error reading float data (%d, %d, %d, %d, %d, %d)",
							x, y, t, dx, dy, dt),
					e);
		}

		if (data == null) {
			throwNullException();
		}
		return data;
	}

	private void throwNullException() throws DeviceException {
		close();
		throw new DeviceException("read null from daserver");
	}


	@Override
	public float[] readFloat(int frame) throws DeviceException {
		float[] data = new float[0];
		int npoints;

		ensureOpen();

		// frame in this case is calibration channel
		npoints = totalFrames;
		try {
			data = daServer.getFloatBinaryData("read " + frame + " 0 0 1 1 " + totalFrames + " " + localEndian
					+ " float from " + handle, npoints);
		} catch (Exception e) {
			throw new DeviceException("Error reading float data, frame: " + frame, e);
		}
		if (data == null) {
			throwNullException();
		}
		return data;
	}

	@Override
	public double[] read(int x, int y, int t, int dx, int dy, int dt) throws DeviceException {
		double[] data = new double[0];
		int npoints = dx * dy * dt;

		ensureOpen();

		try {
			data = daServer.getBinaryData("read " + t + " " + y + " " + x + " " + dt + " " + dy + " " + dx + " "
					+ localEndian + " float from " + handle, npoints);
		} catch (Exception e) {
			throw new DeviceException(
					String.format("Error reading float data (%d, %d, %d, %d, %d, %d)",
							x, y, t, dx, dy, dt),
					e);
		}
		if (data == null) {
			throwNullException();
		}
		return data;
	}

	@Override
	public double[] read(int frame) throws DeviceException {
		double[] data = new double[0];
		int npoints;

		ensureOpen();

		// frame in this case is calibration channel
		npoints = totalFrames;
		try {
			data = daServer.getBinaryData("read " + frame + " 0 0 1 1 " + totalFrames + " " + localEndian + " float from "
					+ handle, npoints);
		} catch (Exception e) {
			throw new DeviceException("Error reading double data, frame: " + frame, e);
		}
		if (data == null) {
			throwNullException();
		}
		return data;
	}

	@Override
	public void output(String file) throws DeviceException {
		String command;
		ensureOpen();

		if (host == null || user == null || password == null || host.equals("") || user.equals("")
				|| password.equals("")) {
			command = "read 0 0 0 " + totalFrames + " " + height + " " + width + " to-local-file '" + file + "' from "
					+ handle + " float " + remoteEndian;
		} else {
			command = "read 0 0 0 " + totalFrames + " " + height + " " + width + " to-remote-file '" + file + "' on '"
					+ host + "' user '" + user + "' password '" + password + "' from " + handle + " float "
					+ remoteEndian;
		}
		sendAndParse("output", command);
	}
}
