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

package uk.ac.gda.devices.detector.xspress3;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.CounterTimer;
import gda.device.DeviceException;
import gda.device.detector.DetectorBase;

/**
 * Returns FF/I0 for an Xspress3 / ion chamber combination. Note this does not operate any hardware so should be used
 * with other detectors in scans
 */
public class Xspress3FFoverI0Detector extends DetectorBase {
	private static final Logger logger = LoggerFactory.getLogger(Xspress3FFoverI0Detector.class);

	private Xspress3 xspress3 = null;
	private CounterTimer ct = null;
	private int i0_channel = 0;

	public Xspress3FFoverI0Detector() {
	}

	@Override
	public void configure() {
		if (isConfigured()) {
			return;
		}
		if (getExtraNames().length == 0)
			this.setExtraNames(new String[] { "FFI0" });
		this.setInputNames(new String[0]);
		if (outputFormat == null || outputFormat.length != 1)
			this.setOutputFormat(new String[] { "%.6f" });
		setConfigured(true);
	}

	@Override
	public Object readout() throws DeviceException {
		Double i0 = getI0();
		Double ff = xspress3.readoutFF();
		Double ffio = ff / i0;
		if (i0 == 0.0 || i0.isNaN() || i0.isInfinite()){
			logger.info("Problem with I0, so set FF/I0 to 0.0");
			ffio = 0.0;
		} else if (ff == 0.0 || ff.isInfinite() || ff.isNaN()){
			logger.info("Problem with FF, so set FF/I0 to 0.0");
			ffio = 0.0;
		}
		return ffio;
	}

	private Double getI0() throws DeviceException {
		// assume that this is a TFGv2 behind where the first column is always the live time, so the next channel will be
		// the I0
		Object out = ct.readout();
		return ((double[]) out )[i0_channel];
	}

	@Override
	public void collectData() throws DeviceException {
		// do nothing as this object only reads out others' data
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return false;
	}

	@Override
	public String getDescription() throws DeviceException {
		return "Ratio of Xspress FF and ion chamber";
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return "unknown";
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return "Scaler";
	}

	@Override
	public int getStatus() throws DeviceException {
		return 0;
	}

	public Xspress3 getXspress3() {
		return xspress3;
	}

	public void setXspress3(Xspress3 xmap) {
		this.xspress3 = xmap;
	}

	public CounterTimer getCounterTimer() {
		return ct;
	}

	public void setCounterTimer(CounterTimer i0CounterTimer) {
		this.ct = i0CounterTimer;
	}

	public void setI0_channel(int i0_channel) {
		this.i0_channel = i0_channel;
	}

	public int getI0_channel() {
		return i0_channel;
	}
}
