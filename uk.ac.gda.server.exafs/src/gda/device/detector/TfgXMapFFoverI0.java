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

import gda.data.nexus.tree.INexusTree;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.CounterTimer;
import gda.device.DeviceException;
import gda.device.XmapDetector;
import gda.factory.Finder;

import org.nexusformat.NexusFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Returns FF/I0 for an Xmap (Vortex) / ion chamber combination. Note this does not operate any hardware so should be used
 * with other detectors in scans
 */
public class TfgXMapFFoverI0 extends DetectorBase implements NexusDetector {
	private static final Logger logger = LoggerFactory.getLogger(TfgXMapFFoverI0.class);

	private XmapDetector xmap = null;
	private String xmapSystemName;

	private CounterTimer ct = null;
	private String ctName;
	private int i0_channel = 0;

	public TfgXMapFFoverI0() {
	}

	@Override
	public void configure() {
		if (xmap == null) {
			if ((xmap = (XmapDetector) Finder.getInstance().find(xmapSystemName)) == null) {
				logger.error("XspressSystem " + xmapSystemName + " not found");
			}
		}
		if (ct == null) {
			logger.debug("Finding: " + ctName);
			if ((ct = (CounterTimer) Finder.getInstance().find(ctName)) == null) {
				logger.error("Scaler " + ctName + " not found");
			}
		}

		if (getExtraNames().length == 0) {
			this.setExtraNames(new String[] { getName() });
		}
		this.setInputNames(new String[0]);
		this.setOutputFormat(new String[] { "%.6f" });

	}

	@Override
	public NexusTreeProvider readout() throws DeviceException {
		Double i0 = getI0();
		Double ff = getFF();
		
		NXDetectorData thisFrame = new NXDetectorData(this);
		INexusTree detTree = thisFrame.getDetTree(getName());

		Double ffio = ff / i0;
		if (i0 == 0.0 || ff == 0.0 || i0.isInfinite() || i0.isNaN() || ff.isInfinite() || ff.isNaN()) {
			ffio = 0.0;
		}

		thisFrame
				.addData(detTree, getExtraNames()[0], new int[] { 1 }, NexusFile.NX_FLOAT64, new Double[] { ffio }, "counts", 1);
		thisFrame.setPlottableValue(getExtraNames()[0], ffio);
		
		return thisFrame;
	}

	private Double getFF() throws DeviceException {
		return xmap.readoutScalerData();
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

	public XmapDetector getXmap() {
		return xmap;
	}

	public void setXmap(XmapDetector xmap) {
		this.xmap = xmap;
	}

	public String getXmapSystemName() {
		return xmapSystemName;
	}

	public void setXmapSystemName(String xmapSystemName) {
		this.xmapSystemName = xmapSystemName;
	}

	public CounterTimer getCounterTimer() {
		return ct;
	}

	public void setCounterTimer(CounterTimer i0CounterTimer) {
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
