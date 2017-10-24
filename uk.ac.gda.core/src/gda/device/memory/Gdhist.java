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

import gda.device.DeviceBase;
import gda.device.DeviceException;
import gda.device.Memory;
import gda.device.detector.DAServer;
import gda.factory.FactoryException;
import gda.factory.Finder;

/**
 * A memory class for the VME Generic data acquisition histogramming memory card implemented using DA.Server
 */
public class Gdhist extends DeviceBase implements Memory {

	private static final Logger logger = LoggerFactory.getLogger(Gdhist.class);

	protected static final String localEndian = "motorola";

	private static final String USER = "User";

	private static final String PASSWORD = "Password";

	private static final String HOST = "Host";

	private static final String TOTALFRAMES = "TotalFrames";

	private static final String REMOTEENDIAN = "Endian";

	protected DAServer daServer;

	private String daServerName;

	protected String startupScript = null;

	protected String openCommand = null;

	private String sizeCommand = null;

	protected int width = 0;

	protected int height = 0;

	private int[] supportedDimensions = new int[] {};

	protected int totalFrames = 1;

	protected int handle = -1;

	protected String user = null;

	protected String password = null;

	protected String host = null;

	protected String remoteEndian = "intel";

	@Override
	public void configure() throws FactoryException {
		if (daServer == null) {
			if ((daServer = (DAServer) Finder.getInstance().find(daServerName)) == null) {
				throw new FactoryException("DAServer " + daServerName + " not found");
			}
		}
		if (openCommand == null) {
			throw new FactoryException("not open command for " + this.getName());
		}
		try {
			ensureOpen();
		} catch (DeviceException e) {
			throw new FactoryException("Error configuring " + getDaServerName(), e);
		}
	}

	@Override
	public void reconfigure() throws FactoryException {
		try {
			close();
		} catch (DeviceException e) {
			throw new FactoryException("Device exception while closing",e);
		}
		configure();
	}

	protected void ensureOpen() throws DeviceException {
		if (handle >= 0) {
			return;
		}
		open();
	}

	/**
	 * send a command and report error/throw exception when operation fails, i.e. daserver replies -1
	 *
	 * @param operation
	 *            descriptive name of the operation attempted for logging
	 * @param command
	 *            command to send to daserver
	 * @return Object
	 * @throws DeviceException
	 */
	protected Object sendAndParse(String operation, String command) throws DeviceException {
		Object obj = null;
		if (!daServer.isConnected()) {
			// throw new DeviceException("daserver not connected");
		}

		if ((obj = daServer.sendCommand(command)) == null) {
			throw new DeviceException("null reply received from daserver during " + operation);
		}

		if(obj.equals("IDLE"))
			return obj;

		if(!(obj instanceof Integer)) {
			return obj;
		}

		if (((Integer) obj).intValue() == -1) {
			logger.error(getName() + ": " + operation + " failed");
			close();
			throw new DeviceException("Ghist " + getName() + " " + operation + " failed");
		}

		return obj;
	}

	protected void open() throws DeviceException {
		String cmd = openCommand;

		if (openCommand.startsWith("gdhist open")) {
			if (width > 0) {
				cmd += " " + width;
			}
			if (height > 0) {
				cmd += " " + height;
			}
		} else {
			logger.error("Invalid open command for " + getName());
			throw new DeviceException("Gdhist: Invalid open command for " + this.getName());
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
	public void close() throws DeviceException {
		if (handle < 0) {
			return;
		}
		daServer.sendCommand("close " + handle);
		handle = -1;
	}

	@Override
	public void clear() throws DeviceException {
		ensureOpen();

		sendAndParse("clear", "clear " + handle);
	}

	@Override
	public void clear(int start, int count) throws DeviceException {
		ensureOpen();

		sendAndParse("clear", "clear " + handle + " " + start + " " + count);
	}

	@Override
	public void clear(int x, int y, int t, int dx, int dy, int dt) throws DeviceException {
		ensureOpen();

		sendAndParse("clear", "clear " + handle + " " + x + " " + y + " " + t + " " + dx + " " + dy + " " + dt);
	}

	@Override
	public void start() throws DeviceException {
		ensureOpen();

		sendAndParse("start", "enable " + handle);
	}

	@Override
	public void stop() throws DeviceException {
		ensureOpen();

		sendAndParse("stop", "disable " + handle);
	}

	public float[] readFloat(int x, int y, int t, int dx, int dy, int dt) throws DeviceException {
		float[] data = new float[0];
		int npoints = dx * dy * dt;

		ensureOpen();
		try {
			data = daServer.getFloatBinaryData("read " + x + " " + y + " " + t + " " + dx + " " + dy + " " + dt + " "
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

	public float[] readFloat(int frame) throws DeviceException {
		float[] data = new float[0];
		int npoints;

		ensureOpen();
		npoints = width * height;
		try {
			data = daServer.getFloatBinaryData("read 0 0 " + frame + " " + width + " " + height + " 1 " + localEndian
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
			data = daServer.getBinaryData("read " + x + " " + y + " " + t + " " + dx + " " + dy + " " + dt + " "
					+ localEndian + " float from " + handle, npoints);
		} catch (Exception e) {
			throw new DeviceException(
					String.format("Error reading double data (%d, %d, %d, %d, %d, %d)",
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

		npoints = width * height;
		try {
			data = daServer.getBinaryData("read 0 0 " + frame + " " + width + " " + height + " 1 " + localEndian
					+ " float from " + handle, npoints);
		} catch (Exception e) {
			throw new DeviceException("Error reading double data, frame: " + frame, e);
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

	/**
	 * @return Returns the daServer.
	 */
	public DAServer getDaServer() {
		return daServer;
	}

	/**
	 * @param daServer
	 *            The daServer to set.
	 */
	public void setDaServer(DAServer daServer) {
		this.daServer = daServer;
	}

	@Override
	public void setDimension(int[] d) throws DeviceException {
		if (d.length != 2)
			throw new DeviceException("only 2d resolutions supported");

		int newWidth = d[0];
		int newHeight = d[1];

		// ensure initial configuration works
		if (width != 0 && height != 0) {
			if (supportedDimensions == null) {
				throw new DeviceException("this detector does not support resolution changes");
			}
			for (int i : d) {
				boolean found = false;
				for (int j : supportedDimensions) {
					if (i == j) {
						found = true;
					}
				}
				if (!found) {
					throw new DeviceException("resolution " + i + " not supported");
				}
			}
		}

		height = newHeight;
		width = newWidth;

		if (handle >= 0) {
			// otherwise that will happen anyway
			close();
			open();
		}
	}

	@Override
	public void output(String file) throws DeviceException {
		String command;
		ensureOpen();

		if (host == null || user == null || password == null || host.equals("") || user.equals("")
				|| password.equals("")) {
			command = "read 0 0 0 " + width + " " + height + " " + totalFrames + " to-local-file '" + file + "' from "
					+ handle + " float " + remoteEndian;
		} else {
			command = "read 0 0 0 " + width + " " + height + " " + totalFrames + " to-remote-file '" + file + "' on '"
					+ host + "' user '" + user + "' password '" + password + "' from " + handle + " float "
					+ remoteEndian;
		}
		sendAndParse("output", command);
	}

	@Override
	public int getMemorySize() throws DeviceException {
		ensureOpen();

		return ((Integer) sendAndParse("getMemorySize", sizeCommand));
	}

	/**
	 * Set attribute values for "User", "Password", "Host", "TotalFrames", "Endian".
	 *
	 * @see gda.device.DeviceBase#setAttribute(java.lang.String, java.lang.Object)
	 */
	@Override
	public void setAttribute(String attributeName, Object value) {
		if (USER.equals(attributeName)) {
			user = (String) value;
		} else if (PASSWORD.equals(attributeName)) {
			password = (String) value;
		} else if (HOST.equals(attributeName)) {
			host = (String) value;
		} else if (TOTALFRAMES.equals(attributeName)) {
			totalFrames = ((Integer) value).intValue();
		} else if (REMOTEENDIAN.equals(attributeName)) {
			remoteEndian = (String) value;
		}
	}

	@Override
	public Object getAttribute(String attributeName) {
		if (attributeName.equals(USER)) {
			return user;
		} else if (attributeName.equals(PASSWORD)) {
			return password;
		} else if (attributeName.equals(HOST)) {
			return host;
		} else if (attributeName.equals(TOTALFRAMES)) {
			return totalFrames;
		} else if (attributeName.equals(REMOTEENDIAN)) {
			return remoteEndian;
		}
		return null;
	}

	@Override
	public int[] getDimension() {
		int[] dims = { width, height };
		return dims;
	}

	/**
	 * set the number of pixel settings the detector can work at. assumes a 2d detector with identical axis.
	 *
	 * @param d
	 */
	public void setSupportedDimensions(int[] d) {
		this.supportedDimensions = d;
	}

	/**
	 * @return a collection of supported Dimensions
	 */
	@Override
	public int[] getSupportedDimensions() {
		return supportedDimensions;
	}

	@Override
	public void write(double[] data, int x, int y, int t, int dx, int dy, int dt) {
		logger.debug("Not implemented by da.server");
	}

	@Override
	public void write(double[] data, int frame) {
		logger.debug("Not implemented by da.server");
	}

	/**
	 * @return width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @param width
	 * @throws DeviceException
	 */
	public void setWidth(int width) throws DeviceException {
		int[] dims = { width, height };
		setDimension(dims);
	}

	/**
	 * @return height
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * @param height
	 * @throws DeviceException
	 */
	public void setHeight(int height) throws DeviceException {
		int[] dims = { width, height };
		setDimension(dims);
	}

	/**
	 * Set the name of the daServer instance
	 *
	 * @param daServerName
	 *            the server name
	 */
	public void setDaServerName(String daServerName) {
		this.daServerName = daServerName;
	}

	/**
	 * Get the name of the daServer instance.
	 *
	 * @return the server name
	 */
	public String getDaServerName() {
		return daServerName;
	}

	/**
	 * Set the startup script command
	 *
	 * @param startupScript
	 *            the startup script command
	 */
	public void setStartupScript(String startupScript) {
		this.startupScript = startupScript;
	}

	/**
	 * Get the startup script command
	 *
	 * @return the startup script command
	 */
	public String getStartupScript() {
		return startupScript;
	}

	/**
	 * Set the command to obtain the size of memory
	 *
	 * @param sizeCommand
	 *            the command to obtain the size of memory
	 */
	public void setSizeCommand(String sizeCommand) {
		this.sizeCommand = sizeCommand;
	}

	/**
	 * Get the command which obtains the size of memory
	 *
	 * @return the command
	 */
	public String getSizeCommand() {
		return sizeCommand;
	}

	/**
	 * Set the command (if necessary) for opening the memory
	 *
	 * @param openCommand
	 *            the open command
	 */
	public void setOpenCommand(String openCommand) {
		this.openCommand = openCommand;
	}

	/**
	 * Get the open command
	 *
	 * @return the command
	 */
	public String getOpenCommand() {
		return openCommand;
	}

	// this method is only for Junit testing
	/**
	 * for use by junit tests
	 * @throws DeviceException
	 */
	protected void setFail() throws DeviceException {
		if (daServer != null && daServer.isConnected()) {
			daServer.sendCommand("Fail");
		}
	}
}
