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

import gda.device.CounterTimer;
import gda.device.DeviceException;
import gda.device.detector.DetectorBase;

/**
 * Returns FF/I0 for an Xspress3 (Vortex) / ion chamber combination. Note this does not operate any hardware so should be used
 * with other detectors in scans
 */
public class Xspress3FFoverI0Detector extends DetectorBase {
//	private static final Logger logger = LoggerFactory.getLogger(Xspress3FFoverI0Detector.class);

	private Xspress3Detector xspress3 = null;
	private CounterTimer ct = null;
	private int i0_channel = 0;

	public Xspress3FFoverI0Detector() {
	}

	@Override
	public void configure() {
		// Should really configure these using Spring XML
		setExtraNames(new String[] { "FFI0" });
		setInputNames(new String[0]);
		setOutputFormat(new String[] { "%.9f" });
	}

	@Override
	public Object readout() throws DeviceException {
		Double i0 = getI0();
		Double ff = getFF();
		Double ffio = ff / i0;
		if (i0 == 0.0 || ff == 0.0 || i0.isInfinite() || i0.isNaN() || ff.isInfinite() || ff.isNaN())
			ffio = 0.0;
		return ffio;
	}

	private double getFF() throws DeviceException {
		Double[] ffs =  xspress3.readoutFF();
		double total = 0;
		for(Double ff : ffs){
			total += ff;
		}
		return total;
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

	public Xspress3Detector getXspress3() {
		return xspress3;
	}

	public void setXspress3(Xspress3Detector xmap) {
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
