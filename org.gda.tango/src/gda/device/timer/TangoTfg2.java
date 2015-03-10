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

package gda.device.timer;

import java.util.ArrayList;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.esrf.Tango.DevFailed;
import fr.esrf.TangoApi.DeviceData;
import gda.device.DeviceException;
import gda.device.TangoDeviceProxy;
import gda.device.Timer;

/**
 * A timer class for the VME time frame generator card implemented using DA.Server
 */
public class TangoTfg2 extends TangoTfg1 {

	private static final Logger logger = LoggerFactory.getLogger(TangoTfg2.class);
	private static int MAXFRAMES = 32767;
	private static final String version = "Version 2";
	private ArrayList<Double> debounceValues;
	private ArrayList<Double> thresholdValues;
	private int drive = 0;
	private int inversion = 0;
	private int startMethod;
	private enum group { 
		help(1<<0), ext_start(1<<1), ext_inh(1<<2), cycles(1<<3), file(1<<4), no_min_20us(1<<5), silent(1<<6),
		sequence(1<<7), auto_rearm(1<<8), ext_falling(1<<9);
		
		private int value;
		
		private group(int value) {
			this.value = value;
		}
		
		public int getValue() {
			return value;
		}
	}

	public enum trig {
		help(1<<0), start(1<<1), pause(1<<2), pause_dead(1<<3), falling(1<<4), debounce(1<<5), threshold(1<<6), now(1<<7),
		raw(1<<8), alternate(1<<9);

		private int value;
				
		private trig(int value) {
			this.value = value;
		}
		
		public int getValue() {
			return value;
		}
		
		public int or(trig ... argin) {
			int result = value;
			for (int i=0; i<argin.length; i++) {
				result |= argin[i].getValue();
			}
			return result;
		}
	}


	@Override
	public void loadFrameSets() throws DeviceException {

		String[] argin = new String[5 + timeFrameProfile.size()];
		totalExptTime = 0;
		totalFrames = 0;
		int qualifiers = group.cycles.getValue();
		if (extStart) {
			qualifiers |= group.ext_start.getValue();
		}
		if (extInh) {
			qualifiers |= group.ext_inh.getValue();
		}
		argin[0] = "" + qualifiers;
		argin[1] = "" + cycles;
		argin[2] = ""; // filename
		argin[3] = ""; // sequence name
		int index = 4;
		for (FrameSet frameSet : timeFrameProfile) {
			totalExptTime += (int) (frameSet.getRequestedLiveTime() + frameSet.getRequestedDeadTime())
					* frameSet.getFrameCount();
			totalFrames += frameSet.getFrameCount();
			argin[index++] = "" + frameSet.getFrameCount() + " " + frameSet.getRequestedDeadTime() / 1000 + " "
					+ frameSet.getRequestedLiveTime() / 1000 + " " + frameSet.getDeadPort() + " "
					+ frameSet.getLivePort() + " " + frameSet.getDeadPause() + " " + frameSet.getLivePause();
		}
		argin[index] = "-1 0 0 0 0 0 0";
		try {
			DeviceData args = new DeviceData();
			args.insert(argin);
			tangoDeviceProxy.command_inout("setupGroups", args);
		} catch (DevFailed e) {
			DeviceException ex = new DeviceException(e.errors[0].desc);
			logger.error(ex.getMessage());
			throw ex;
		}
		totalExptTime *= cycles;
		framesLoaded = true;
		notifyIObservers(this, timeFrameProfile);
	}

	/**
	 * Count the specified time (in ms)
	 */
	@Override
	public synchronized void countAsync(double time) {
		// The generate command expects cycles (integer)
		// frames(integer) deadTime (seconds, double) liveTime
		// (seconds, double) pause (integer). The incoming time is in mS.
		totalExptTime = (int) time * cycles;
		try {
			double[] argin = new double[6];
			argin[0] = 0;
			argin[1] = 1;
			argin[2] = 1;
			argin[3] = 0.001;
			argin[4] = time / 1000.0;
			argin[5] = 0;
			DeviceData args = new DeviceData();
			args.insert(argin);
			tangoDeviceProxy.command_inout("generate", args);
			tangoDeviceProxy.command_inout("start");
		} catch (DevFailed e) {
			DeviceException ex = new DeviceException(e.errors[0].desc);
			logger.error(ex.getMessage());
		}
		Date d = new Date();
		startTime = d.getTime();
		elapsedTime = 0;
		started = true;
		notify();
	}

	public void setDriveAndInversion(int drv, int inv) throws DeviceException {
		int[] argin = new int[2];
		argin[0] = inv & 0xFF;
		argin[1] = drv & 0xFF;
		try {
			DeviceData args = new DeviceData();
			args.insert(argin);
			tangoDeviceProxy.isAvailable();
			tangoDeviceProxy.command_inout("setupPort", args);
		} catch (DevFailed e) {
			DeviceException ex = new DeviceException(e.errors[0].desc);
			logger.error(ex.getMessage());
			throw ex;
		}
	}

	public void setStartMethod(int startTrig) throws DeviceException {
		double[] argin = new double[5];
		argin[4] = 0; // unused here. Alternate trigger input value
		if (startTrig == 0) {
			argin[0] = trig.start.getValue();
			argin[1] = 0; // Trigger input number 1..16
			argin[2] = 0; // Debounce value
			argin[3] = 0; // Threshold value
			extStart = false;
		} else if (startTrig < 17) {
			double debounce = debounceValues.get(startTrig - 1);
			double threshold = thresholdValues.get(startTrig - 1);
			if (debounceValues != null && debounce != Double.NaN && debounce != 0.0) {
				if (startTrig == 16 && thresholdValues != null && threshold != Double.NaN) {
					argin[0] = trig.start.or(trig.debounce, trig.threshold);
					argin[1] = startTrig;
					argin[2] = debounce;
					argin[3] = threshold;
				} else {
					argin[0] = trig.start.or(trig.debounce);
					argin[1] = startTrig;
					argin[2] = debounce;
					argin[3] = 0;
				}
			} else {
				if (startTrig == 16 && thresholdValues != null && threshold != Double.NaN) {
					argin[0] = trig.start.or(trig.threshold);
					argin[2] = 0;
					argin[3] = threshold;
				} else {
					argin[0] = trig.start.getValue();
					argin[1] = 0;
					argin[2] = 0;
					argin[3] = 0;
				}
			}
			extStart = true;
		} else {
			// there is only one debounce value per trigger channel, hence the -16
			double debounce = debounceValues.get(startTrig - 17);
			double threshold = thresholdValues.get(startTrig - 17);
			if (debounceValues != null && debounce != Double.NaN && debounce != 0.0) {
				if (startTrig == 32 && thresholdValues != null && threshold != Double.NaN) {
					argin[0] = trig.start.or(trig.debounce, trig.threshold, trig.falling);
					argin[1] = startTrig - 16;
					argin[2] = debounce;
					argin[3] = threshold;
				} else {
					argin[0] = trig.start.or(trig.debounce, trig.falling);
					argin[1] = startTrig - 16;
					argin[2] = debounce;
					argin[3] = 0;
				}
			} else {
				if (startTrig == 16 && thresholdValues != null && threshold != Double.NaN) {
					argin[0] = trig.start.or(trig.threshold, trig.falling);
					argin[1] = startTrig - 16;
					argin[2] = 0;
					argin[3] = threshold;
				} else {
					argin[0] = trig.start.or(trig.falling);
					argin[1] = startTrig - 16;
					argin[2] = 0;
					argin[3] = 0;
				}
			}
			extStart = true;
		}
		try {
			DeviceData args = new DeviceData();
			args.insert(argin);
			tangoDeviceProxy.isAvailable();
			tangoDeviceProxy.command_inout("setupTrig", args);
		} catch (DevFailed e) {
			DeviceException ex = new DeviceException(e.errors[0].desc);
			logger.error(ex.getMessage());
			throw ex;
		}
	}

	@Override
	public int getMaximumFrames() {
		return MAXFRAMES;
	}

	public void setMaximumFrames(int max) {
		MAXFRAMES = max;
	}
	
	/**
	 * Set attribute values for "Ext-Start", "Ext-Inhibit", "VME-Start" and "Auto-Continue"
	 * 
	 * @param attributeName
	 *            the attribute name
	 * @param value
	 *            the attribute value
	 * @throws DeviceException 
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void setAttribute(String attributeName, Object value) throws DeviceException {
		if ("Debounce".equals(attributeName)) {
			debounceValues = (ArrayList<Double>) value;
		} else if ("Threshold".equals(attributeName)) {
			thresholdValues = (ArrayList<Double>) value;
		} else if ("Inversion".equals(attributeName)) {
			inversion = (Integer) value;
			setDriveAndInversion(drive, inversion);
		} else if ("Drive".equals(attributeName)) {
			drive = (Integer) value;
			setDriveAndInversion(drive, inversion);
		} else if ("Start-Method".equals(attributeName)) {
			startMethod = (Integer) value;
			setStartMethod(startMethod);

		}
		super.setAttribute(attributeName, value);
	}

	
	@Override
	public Object getAttribute(String attributeName) throws DeviceException {
		if ("Debounce".equals(attributeName)) {
			return debounceValues;
		} else if ("Threshold".equals(attributeName)) {
			return thresholdValues;
		} else if ("Inversion".equals(attributeName)) {
			return inversion;
		} else if ("Drive".equals(attributeName)) {
			return drive;
		} else if ("Start-Method".equals(attributeName)) {
			return startMethod;
		}
		return super.getAttribute(attributeName);
	}
}
