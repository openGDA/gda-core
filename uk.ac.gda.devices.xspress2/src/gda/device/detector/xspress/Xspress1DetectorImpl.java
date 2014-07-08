/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.device.detector.xspress;

import gda.device.DeviceException;
import gda.device.detector.DAServer;
import gda.factory.FactoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Xspress1DetectorImpl implements XspressDetectorImpl {

	private static final Logger logger = LoggerFactory.getLogger(Xspress1DetectorImpl.class);
	private String xspressSystemName = null;
	private DAServer daServer;
	private String mcaOpenCommand = null;
	private String scalerOpenCommand = null;
	private int mcaHandle = -1;
	private int scalerHandle = -1;
	private String startupScript = null;
	private int numberOfDetectors = -1;
	private int numberOfScalers = 4;

	public Xspress1DetectorImpl() {
	}

	@Override
	public void configure() throws FactoryException {
		try {
			close();
			doStartupScript();
			open();
		} catch (DeviceException e) {
			throw new FactoryException(e.getMessage(), e);
		}
	}

	public DAServer getDaServer() {
		return daServer;
	}

	public void setDaServer(DAServer daServer) {
		this.daServer = daServer;
	}

	public String getStartupScript() {
		return startupScript;
	}

	public void setStartupScript(String startupScript) {
		this.startupScript = startupScript;
	}

	public void setMcaOpenCommand(String mcaOpenCommand) {
		this.mcaOpenCommand = mcaOpenCommand;
	}

	public String getMcaOpenCommand() {
		return mcaOpenCommand;
	}

	public void setScalerOpenCommand(String scalerOpenCommand) {
		this.scalerOpenCommand = scalerOpenCommand;
	}

	public String getScalerOpenCommand() {
		return scalerOpenCommand;
	}

	public void setNumberOfDetectors(int numberOfDetectors) {
		this.numberOfDetectors = numberOfDetectors;
	}

	@Override
	public int getNumberOfDetectors() {
		return numberOfDetectors;
	}

	private void open() throws DeviceException {
		Object obj;
		if (daServer != null && daServer.isConnected()) {
			if (mcaOpenCommand != null) {
				if ((obj = daServer.sendCommand(mcaOpenCommand)) != null) {
					mcaHandle = ((Integer) obj).intValue();
					if (mcaHandle < 0) {
						throw new DeviceException("Failed to create the mca handle");
					}
					logger.info("Xspress1System: open() using mcaHandle " + mcaHandle);
				}
			}

			if (scalerOpenCommand != null) {
				if ((obj = daServer.sendCommand(scalerOpenCommand)) != null) {
					scalerHandle = ((Integer) obj).intValue();
					if (scalerHandle < 0) {
						throw new DeviceException("Failed to create the scaler handle");
					}
					logger.info("Xspress1System: open() using scalerHandle " + scalerHandle);
				}
			}
		}
	}

	@Override
	public void close() throws DeviceException {
		if (mcaHandle >= 0) {
			daServer.sendCommand("close " + mcaHandle);
			mcaHandle = -1;
		}
		if (scalerHandle >= 0) {
			daServer.sendCommand("close " + scalerHandle);
			scalerHandle = -1;
		}
	}

	@Override
	public void setCollectionTime(int time) throws DeviceException {
		// to be implemented
	}

	@Override
	public void setWindows(int detector, int winStart, int winEnd) throws DeviceException {
		Object obj = null;
		String cmd = "xspress set-windows '" + xspressSystemName + "' " + detector+ " " + winStart + " " + winEnd;
		if (daServer != null && daServer.isConnected()) {
			obj = daServer.sendCommand(cmd);
			if (((Integer) obj).intValue() < 0) {
				throw new DeviceException("Xspress1System error setting windows: ");
			}
		}
	}

	@Override
	public void clear() throws DeviceException {
		if (mcaHandle < 0 || scalerHandle < 0) {
			open();
		}
		if (mcaHandle >= 0) {
			sendCommand("clear ", mcaHandle);
		}
		if (scalerHandle >= 0) {
			sendCommand("clear ", scalerHandle);
		}
	}

	@Override
	public void start() throws DeviceException {
		if (mcaHandle < 0 || scalerHandle < 0) {
			open();
		}
		if (mcaHandle >= 0 && daServer != null && daServer.isConnected()) {
			sendCommand("enable ", mcaHandle);
		}
		if (scalerHandle >= 0 && daServer != null && daServer.isConnected()) {
			sendCommand("enable ", scalerHandle);
		}
	}

	@Override
	public void stop() throws DeviceException {
		if (mcaHandle < 0 || scalerHandle < 0) {
			open();
		}
		if (mcaHandle >= 0 && daServer != null && daServer.isConnected()) {
			sendCommand("disable ", mcaHandle);
		}
		if (scalerHandle >= 0 && daServer != null && daServer.isConnected()) {
			sendCommand("disable ", scalerHandle);
		}
	}

	@Override
	public void reconfigure() throws DeviceException {
		// A real system needs a connection to a real da.server via a DAServer object.
		logger.debug("Xspress1System.reconfigure(): reconnecting to: " + daServer.getName());
		daServer.reconnect();
		// If everything has been found send the open commands.
		if (daServer != null) {
			open();
		}
	}

	@Override
	public int[] readoutHardwareScalers(int startFrame, int numberOfFrames) throws DeviceException {
		int[] value = null;
		if (scalerHandle < 0) {
			open();
		}
		if (scalerHandle >= 0 && daServer != null && daServer.isConnected()) {
			try {
			value = daServer.getIntBinaryData("read 0 0 " + startFrame + " " + numberOfScalers + " "
					+ numberOfDetectors + " " + numberOfFrames + " from " + scalerHandle + " raw motorola",
					numberOfDetectors * numberOfScalers * numberOfFrames);
			} catch(Exception e) {
				throw new DeviceException(e.getMessage(), e);
			}
		}
		return value;
	}

	/**
	 * Readout full mca for every detector element and specified time frame
	 * 
	 * @param startFrame
	 *            time frame to read
	 * @param numberOfFrames
	 * @return mca data
	 * @throws DeviceException
	 */
	@Override
	public synchronized int[] readoutMca(int detector, int startFrame, int numberOfFrames, int mcaSize) throws DeviceException {
		int[] value = null;
		if (mcaHandle < 0) {
			open();
		}
		if (mcaHandle >= 0 && daServer != null && daServer.isConnected()) {
			try {
			value = daServer.getIntBinaryData("read 0 " + detector + " " + startFrame + " " + mcaSize + " 1 "
					+ numberOfFrames + " from " + mcaHandle + " raw motorola", numberOfDetectors
					* mcaSize * numberOfFrames);
			} catch(Exception e) {
				throw new DeviceException(e.getMessage(), e);
			}
		}
		return value;
	}

	/**
	 * execute the startup script on da.server
	 * 
	 * @throws DeviceException
	 */
	private void doStartupScript() throws DeviceException {
		Object obj = null;
		if (daServer != null && daServer.isConnected()) {
			if (startupScript != null) {
				if ((obj = daServer.sendCommand(startupScript)) == null) {
					throw new DeviceException("Null reply received from daserver during " + startupScript);
				} else if (((Integer) obj).intValue() == -1) {
					throw new DeviceException("Xspress1Impl: " + startupScript + " failed");
				}
			}
		}
	}

	private synchronized void sendCommand(String command, int handle) throws DeviceException {
		Object obj;
		if ((obj = daServer.sendCommand(command + handle)) == null) {
			throw new DeviceException("Null reply received from daserver during " + command);
		} else if (((Integer) obj).intValue() == -1) {
			logger.error("Xspress1Impl: " + command + " failed");
			close();
			throw new DeviceException("Xspress1Impl " + command + " failed");
		}
	}

	// this method is only for Junit testing
	/**
	 * for use by junit tests
	 */
	protected void setFail()  throws DeviceException {
		if (daServer != null && daServer.isConnected()) {
			daServer.sendCommand("Fail");
		}
	}
}
