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

package gda.device.detector.countertimer;

import gda.device.CounterTimer;
import gda.device.DeviceException;
import gda.device.Xspress;
import gda.device.xspress.DetectorReading;
import gda.device.xspress.Xspress2System;
import gda.factory.FactoryException;
import gda.factory.Finder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a Tfg and XspressSystem combination. Since the Tfg will generally also be part of a TfgScaler combination
 * there is a slave mode. In this mode methods which set things on the Tfg do nothing.
 */
public class TfgXspress extends TFGCounterTimer implements CounterTimer {
	
	private static final Logger logger = LoggerFactory.getLogger(TfgXspress.class);

	private Xspress xspressSystem = null;

	private String xspressSystemName;

	private boolean readoutFileName = false;

	@Override
	public void configure() throws FactoryException {
		logger.debug("Finding: " + xspressSystemName);
		if ((xspressSystem = (Xspress) Finder.getInstance().find(xspressSystemName)) == null) {
			logger.error("XspressSystem " + xspressSystemName + " not found");
		}
		super.configure();
		isTFGv2 = false;
	}

	public void setXspressSystemName(String xspressSystemName) {
		this.xspressSystemName = xspressSystemName;
	}

	public String getXspressSystemName() {
		return xspressSystemName;
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return (getReadoutMode() == 0) ? true : false;
	}

	public int getTotalChans() throws DeviceException {
		return xspressSystem.getNumberOfDetectors();
	}

	public void countAsync(double time) throws DeviceException {
		if (xspressSystem instanceof Xspress2System) {
			((Xspress2System) xspressSystem).clear();
			((Xspress2System) xspressSystem).start();
		}
		if (!slave)
			timer.countAsync(time);
	}

	public double[] readChans() throws DeviceException {
		// Is this right? Check that it actually works with real detector
		// system.
		return xspressSystem.readFrame(0, xspressSystem.getNumberOfDetectors(), 0);
	}

	@Override
	public double[] readChannel(int startFrame, int frameCount, int channel) throws DeviceException {
		DetectorReading dr = xspressSystem.readDetector(channel);
		double[] values = new double[frameCount];
		for (int i = 0; i < frameCount; i++)
			values[i] = dr.getWindowed();

		return values;
	}

	@Override
	public double[] readFrame(int startChannel, int channelCount, int frame) throws DeviceException {
		// NB the xspressSystem counts the frames
		// from 0 but ContinuousScan (which uses this method) starts from 1.
		return xspressSystem.readFrame(startChannel, channelCount, frame - 1);
	}

	public String readFrameFile(int startChannel, int channelCount, int frame) throws DeviceException {
		String value = "wrong type of xspress";
		if (xspressSystem instanceof Xspress2System) {
			value = ((Xspress2System) xspressSystem).readFrameFile(startChannel, channelCount, frame);
		}
		return value;
	}

	@Override
	public void setAttribute(String attributeName, Object value) throws DeviceException {
		if (!slave)
			timer.setAttribute(attributeName, value);
		xspressSystem.setAttribute(attributeName, value);
	}

	@Override
	public Object getAttribute(String attributeName) throws DeviceException {
		Object obj;
		if ((obj = timer.getAttribute(attributeName)) == null)
			obj = xspressSystem.getAttribute(attributeName);
		return obj;
	}

	@Override
	public void collectData() throws DeviceException {
		countAsync(collectionTime);
	}

	@Override
	public Object readout() throws DeviceException {
		return xspressSystem.readout();
	}

	/**
	 * Test if the readout is a file name
	 * 
	 * @return true if a file name is readout
	 */
	public boolean isReadoutFileName() {
		return readoutFileName;
	}

	/**
	 * Set the mode of readout
	 * 
	 * @param readoutFileName
	 *            set true for file name to be readout
	 */
	public void setReadoutFileName(boolean readoutFileName) {
		this.readoutFileName = readoutFileName;
		try {
			// NB it is safe to use the Xspress2System values here because
			// the
			// method in Xspress1System does nothing. However it is not very
			// elegant.
			xspressSystem.setReadoutMode(readoutFileName ? Xspress2System.READOUT_FILE
					: Xspress2System.READOUT_WINDOWED);
		} catch (DeviceException e) {
			logger.error("TfgXspress.setReadoutFileName(): DeviceException setting readout mode");
		}
	}

	public int getReadoutMode() {
		return ((Xspress2System) xspressSystem).getReadoutMode();
	}

	public void clearAndStart() throws DeviceException {
		// NB This starts as well as clearing
		if (xspressSystem instanceof Xspress2System) {
			// ((Xspress2System) xspressSystem).stop();
			((Xspress2System) xspressSystem).clear();
			((Xspress2System) xspressSystem).start();
		}
	}

	@Override
	public String getDescription() throws DeviceException {
		return "XSPRESS based CounterTimer";
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return "unknown";
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return "CounterTimer";
	}
}
