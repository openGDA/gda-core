/*-
 * Copyright © 2011 Diamond Light Source Ltd., Science and Technology
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

import gda.device.DeviceException;

import java.util.ArrayList;

/**
 * A timer class for the VME time frame generator (Version 2) card implemented using DA.Server
 */
public class Etfg extends Tfg {
	private static int MAXFRAMES = 32767;
	private static final String version = "Version 2";
	protected ArrayList<Double> debounceValues;
	protected ArrayList<Double> thresholdValues;
	protected int drive = 0;
	protected int inversion = 0;
	protected int startMethod;

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

	public void setDriveAndInversion(int drv, int inv) throws DeviceException {
		checkOKToSendCommand();
		inversion = inv & 0xFF;
		drive = drv & 0xFF;
		daServer.sendCommand(String.format("tfg setup-port %d %d", inversion, drive));
	}

	public void setStartMethod(int startTrig) throws DeviceException {
		if (daServer != null && daServer.isConnected()) {
			startMethod = startTrig;
			if (startMethod == 0) {
				daServer.sendCommand("tfg setup-trig start");
				extStart = false;
				vmeStart = true;
			} else if (startMethod < 17) {
				double debounce = debounceValues.get(startMethod - 1);
				double threshold = thresholdValues.get(startMethod - 1);
				if (debounceValues != null && debounce != Double.NaN && debounce != 0.0) {
					if (startMethod == 16 && thresholdValues != null && threshold != Double.NaN) {
						daServer.sendCommand("tfg setup-trig " + startMethod + " start debounce " + debounce
								+ " threshold " + threshold);
					} else {
						daServer.sendCommand("tfg setup-trig " + startMethod + " start debounce " + debounce);
					}
				} else {
					if (startMethod == 16 && thresholdValues != null && threshold != Double.NaN) {
						daServer.sendCommand("tfg setup-trig " + startMethod + " start" + " threshold " + threshold);
					} else {
						daServer.sendCommand("tfg setup-trig " + startMethod + " start");
					}
				}
				extStart = true;
				vmeStart = false;
			} else {
				// there is only one debounce value per trigger channel, hence the -16
				double debounce = debounceValues.get(startMethod - 17);
				double threshold = thresholdValues.get(startMethod - 17);
				if (debounceValues != null && debounce != Double.NaN && debounce != 0.0) {
					if (startMethod == 32 && thresholdValues != null && threshold != Double.NaN) {
						daServer.sendCommand("tfg setup-trig " + (startMethod - 16) + " start falling debounce "
								+ debounce + " threshold " + threshold);
					} else {
						daServer.sendCommand("tfg setup-trig " + (startMethod - 16) + " start falling debounce "
								+ debounce);
					}
				} else {
					if (startMethod == 16 && thresholdValues != null && threshold != Double.NaN) {
						daServer.sendCommand("tfg setup-trig " + (startMethod - 16) + " start falling" + " threshold "
								+ threshold);
					} else {
						daServer.sendCommand("tfg setup-trig " + (startMethod - 16) + " start falling");
					}
				}
				extStart = true;
				vmeStart = false;
			}
		}
	}
	
	@Override
	public Object getAttribute(String attributeName) {
		if ("Version".equals(attributeName)) {
			return version;
		} else if ("Debounce".equals(attributeName)) {
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