/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package gda.device.detector.nxdetector.plugin.areadetector;

import gda.device.CounterTimer;
import gda.device.DeviceException;
import gda.device.detector.NXDetector;
import gda.device.detector.NXDetectorData;
import gda.device.detector.nxdata.NXDetectorDataAppender;
import gda.device.detector.nxdata.NXDetectorDataDoubleAppender;
import gda.device.detector.nxdetector.plugin.NullNXPlugin;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Vector;

/**
 * Calculate FFI0 for an NXDetector (medipix) using ROI counts for FF and ionchambers for I0.
 * (getI0() method is same as used in TfgFFoverI0).
 * @since 3/2/2016
 * @author Iain Hall
 */
public class ADRoiCountsI0 extends NullNXPlugin {

	private NXDetector nxDetector = null;
	private CounterTimer ct = null;
	private String roiName;
	private int i0_channel = 0;

	public CounterTimer getCt() {
		return ct;
	}

	public void setCt(CounterTimer ct) {
		this.ct = ct;
	}

	public int getI0_channel() {
		return i0_channel;
	}

	public void setI0_channel(int i0_channel) {
		this.i0_channel = i0_channel;
	}

	@Override
	public String getName() {
		return "roi_counts_over_i0";
	}

	@Override
	public List<String> getInputStreamNames() {
		return Arrays.asList("FFI1" );
	}

	@Override
	public List<String> getInputStreamFormats() {
		return Arrays.asList("%f");
	}

	@Override
	public List<NXDetectorDataAppender> read(int maxToRead) throws NoSuchElementException, InterruptedException,
			DeviceException {
	return Arrays.asList((NXDetectorDataAppender) new ADTotalCountsROIAppender());
	}

	class ADTotalCountsROIAppender implements NXDetectorDataAppender {

		@Override
		public void appendTo(NXDetectorData data, String detectorName) throws DeviceException {

			double roiCounts = 0, i0Counts = 0;

			try {
				Double[] values = data.getDoubleVals();
				if ( values.length > 2 )
					roiCounts = values[2];
				i0Counts = getI0();

			} catch (Exception e) {
				throw new DeviceException("Exception reading out roi count from NXDetectorData structure.", e);
			}

			double ffi0=1.0;
			if ( i0Counts > 0 )
				ffi0 = roiCounts/i0Counts;

			List<Double> values = new Vector<Double>();
			values.add(ffi0);

			// Make double appender and it use it for the actual appending
			NXDetectorDataDoubleAppender doubleAppender = new NXDetectorDataDoubleAppender(getInputStreamNames(),
					values);
			doubleAppender.appendTo(data, detectorName);
		}

		double readValue(NXDetectorData data, String fieldName) {
			Double[] values = data.getDoubleVals();
			String[] extraNames = data.getExtraNames();
			int ourValueIndex = Arrays.asList(extraNames).indexOf(fieldName);
			return values[ourValueIndex];
		}
	}

	public Double getI0() throws DeviceException {
		if ( ct != null ) {
			Object out = ct.readout();
			double []arr = (double[])out;
			return arr[i0_channel];
		} else
			return 0.0;
	}

	public NXDetector getNxDetector() {
		return nxDetector;
	}

	public void setNxDetector(NXDetector nxDetector) {
		this.nxDetector = nxDetector;
	}

	public String getRoiName() {
		return roiName;
	}

	public void setRoiName(String roiName) {
		this.roiName = roiName;
	}

	public CounterTimer getCounterTimer() {
		return ct;
	}

	public void setCounterTimer(CounterTimer i0CounterTimer) {
		this.ct = i0CounterTimer;
	}
}
