/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

import gda.device.DeviceException;
import gda.device.Memory;
import gda.device.XmapDetector;
import gda.device.detector.countertimer.TfgScaler;

/**
 * For I20 XES experiments, reads out the I1 ion chamber and calculates If/I1.
 * <p>
 * Should use the XesScanDataPointFormatter to properly format the resulting ascii files.
 * <p>
 * Different flavours of this class will be needs if detectors other than the Vortex is used in an xes scan
 */
public class XesIf extends DetectorBase {

	private XmapDetector xmap = null;
	private TfgScaler scaler = null;
	private Memory gdhist = null;
	private int ifChannelNumber;
	
	public XesIf() {
	}
	
	@Override
	public void configure() {
		this.setExtraNames(new String[] { "I1","FFI1","Time" });
		this.setInputNames(new String[0]);
		this.setOutputFormat(new String[] { "%.4f","%.4f","%6.4g" });
	}


	@Override
	public void collectData() throws DeviceException {
		scaler.countAsync(collectionTime * 1000); // convert from seconds (Detector interface) to milliseconds (CounterTimer interface)
	}

	@Override
	public Double[] readout() throws DeviceException {
		double[] i1_time = getI1Time();
		Double ff = getFF();
		return new Double[]{ i1_time[0], ff / i1_time[0], i1_time[1]};
	}

	private Double getFF() throws DeviceException {
		// currently only expect one value from this class
		return xmap.readoutScalerData();
	}

	private double[] getI1Time() throws DeviceException {
		// read out the full memory of the memory and select the If channel
		double[] read = gdhist.read(0, 0, 0, gdhist.getDimension()[0], 1, 1);
		return new double[] {read[ifChannelNumber],read[0]};
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return false;
	}

	@Override
	public String getDescription() throws DeviceException {
		return "Xes ion chamber and ratio with FF";
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

	public TfgScaler getScaler() {
		return scaler;
	}

	public void setScaler(TfgScaler scaler) {
		this.scaler = scaler;
	}

	public void setGdhist(Memory gdhist) {
		this.gdhist = gdhist;
	}

	public Memory getGdhist() {
		return gdhist;
	}

	public void setIfChannelNumber(int ifChannelNumber) {
		this.ifChannelNumber = ifChannelNumber;
	}

	public int getIfChannelNumber() {
		return ifChannelNumber;
	}

}
