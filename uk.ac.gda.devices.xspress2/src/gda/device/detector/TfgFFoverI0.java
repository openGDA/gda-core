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

package gda.device.detector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.DeviceException;
import gda.device.detector.countertimer.TfgScaler;
import gda.factory.Finder;

/**
 * Returns FF/I0 for an xspress / ion chamber combination. Note this does not operate any hardware so should be used
 * with other detectors in scans
 */
public class TfgFFoverI0 extends DetectorBase implements NexusDetector {
	private static final Logger logger = LoggerFactory.getLogger(TfgFFoverI0.class);
	private NexusDetector xspress = null;
	private String xspressSystemName;
	private TfgScaler ct = null;
	private String ctName;
	private int i0_channel = 0;

	public TfgFFoverI0() {
	}

	@Override
	public void configure() {
		if (xspress == null)
			if ((xspress = (NexusDetector) Finder.getInstance().find(xspressSystemName)) == null)
				logger.error("XspressSystem " + xspressSystemName + " not found");
		if (ct == null) {
			logger.debug("Finding: " + ctName);
			if ((ct = (TfgScaler) Finder.getInstance().find(ctName)) == null)
				logger.error("Scaler " + ctName + " not found");
		}

		this.setExtraNames(new String[] { "FFI0" });
		this.setInputNames(new String[0]);
		if (outputFormat == null || outputFormat.length != 1)
			this.setOutputFormat(new String[] { "%.9f" });
	}

	@Override
	public NexusTreeProvider readout() throws DeviceException {
		Double i0 = getI0();
		Double ff = getFF();
		NXDetectorData thisFrame = new NXDetectorData(this);
		INexusTree detTree = thisFrame.getDetTree(getName());
		double ffio = ff / i0;
		if (i0 == 0.0 || ff == 0.0 || i0.isInfinite() || i0.isNaN() || ff.isInfinite() || ff.isNaN())
			ffio = 0.0;
		NXDetectorData.addData(detTree, "FFI0", new NexusGroupData(ffio), "counts", 1);
		thisFrame.setPlottableValue("FFI0", ffio);
		return thisFrame;
	}

	private Double getFF() throws DeviceException {
		NXDetectorData data = (NXDetectorData) xspress.readout();
		Double[] vals = data.getDoubleVals();
		String[] names = data.getExtraNames();
		int column = 0;
		for (int i = 0; i < names.length; i++) {
			if (names[i].equals("FF")) {
				column = i;
				break;
			}
		}
		return vals[column];
	}

	public Double getI0() throws DeviceException {
		// assume that this is a TFv2 behind where the first column is always the live time, so the next channel will be
		// the I0
		Object out = ct.readout();
		return ((double[]) out)[i0_channel];
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
		return "Ratio of Xspress FF and I0 ion chamber";
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

	public NexusDetector getXspress() {
		return xspress;
	}

	public void setXspress(NexusDetector xspress) {
		this.xspress = xspress;
	}

	public String getXspressSystemName() {
		return xspressSystemName;
	}

	public void setXspressSystemName(String xspressSystemName) {
		this.xspressSystemName = xspressSystemName;
	}

	public TfgScaler getCounterTimer() {
		return ct;
	}

	public void setCounterTimer(TfgScaler i0CounterTimer) {
		this.ct = i0CounterTimer;
	}

	public String getScalerName() {
		return ctName;
	}

	public void setScalerName(String scalerName) {
		this.ctName = scalerName;
	}

	public void setI0_channel(int i0_channel) {
		this.i0_channel = i0_channel;
	}

	public int getI0_channel() {
		return i0_channel;
	}
}
