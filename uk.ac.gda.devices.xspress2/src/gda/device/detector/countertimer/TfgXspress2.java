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
import gda.device.detector.xspress.XspressDetector;
import gda.factory.FactoryException;
import gda.factory.Finder;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adapter class for the gda.device.detector.xspress.Xspress2System so that it acts as a CounterTimer.
 * <p>
 * Used for returning scaler data from the Xspress2 as an array of doubles rather than a Nexus tree.
 * <p>
 * This has a slave mode for when its used in the same scan as another class which triggers the tfg.
 */
public class TfgXspress2 extends TFGCounterTimer implements CounterTimer {

	private static final Logger logger = LoggerFactory.getLogger(TfgXspress2.class);

	protected XspressDetector xspress = null;

	private String xspressSystemName;

	@Override
	public void configure() throws FactoryException {
		if (xspress == null) {
			if ((xspress = (XspressDetector) Finder.getInstance().find(xspressSystemName)) == null) {
				logger.error("XspressSystem " + xspressSystemName + " not found");
			}
		}
		super.configure();
		isTFGv2 = true;
	}

	/**
	 * Set the Xspress system name
	 * 
	 * @param xspressSystemName
	 *            the Xspress system name
	 */
	public void setXspressSystemName(String xspressSystemName) {
		this.xspressSystemName = xspressSystemName;
	}

	/**
	 * Get the Xspress system name
	 * 
	 * @return the Xspress system name.
	 */
	public String getXspressSystemName() {
		return xspressSystemName;
	}

	/**
	 * @return Returns the xspress.
	 */
	public XspressDetector getXspress() {
		return xspress;
	}

	/**
	 * @param xspress
	 *            The xspress to set.
	 */
	public void setXspress(XspressDetector xspress) {
		this.xspress = xspress;
	}

	@Override
	public Object readout() throws DeviceException {
		return xspress.readoutScalerData();
	}

	@Override
	public void collectData() throws DeviceException {
		// only if not in slave mode
		if (!slave && xspress != null && timer != null) {
			if (timer.getAttribute("TotalFrames").equals(0)) {
				xspress.clear();
				xspress.start();
				timer.countAsync(collectionTime);
			} else {
				// if running with frames then simply call cont
				timer.restart();
			}
		}
	}

	@Override
	public double[] readChannel(int startFrame, int frameCount, int channel) throws DeviceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double[] readFrame(int startChannel, int channelCount, int frame) throws DeviceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void restart() throws DeviceException {
		xspress.stop();
		xspress.start();
	}

	@Override
	public void start() throws DeviceException {
		xspress.start();
		if (!slave & timer != null) {
			timer.start();
		}
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return false;
	}

	/**
	 * Override DetectorBase to work within scans
	 */
	@Override
	public Object getPosition() throws DeviceException {
		Object ob = this.readout();
		if (slave) {
			return ob;
		}
		double[] rois = (double[]) ob;
		return ArrayUtils.addAll(new double[] { collectionTime }, rois);

	}

	/**
	 * Override ScannableBase to work within scans
	 */
	@Override
	public String[] getInputNames() {
		if (slave) {
			return new String[] {};
		}
		return new String[] { "time" };
	}

	/**
	 * Override scannablebase to work in scans
	 */
	@Override
	public String[] getExtraNames() {
		return this.xspress.getExtraNames();
	}

	@Override
	public String[] getOutputFormat() {

		String[] extraNames = getExtraNames();
		String[] inputNames = getInputNames();
		if (slave && this.outputFormat.length == extraNames.length) {
			return this.outputFormat;
		} else if (this.outputFormat.length == extraNames.length + inputNames.length) {
			return this.outputFormat;
		}

		String[] formats;

		if (slave) {
			formats = new String[extraNames.length];
		} else {
			formats = new String[extraNames.length + inputNames.length];
		}

		if (slave) {
			formats[0] = "%9.2f";
		} else {
			formats[0] = this.outputFormat[0];
		}
		for (int i = 1; i < formats.length; i++) {
			formats[i] = "%9.2f";
		}

		return formats;
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
