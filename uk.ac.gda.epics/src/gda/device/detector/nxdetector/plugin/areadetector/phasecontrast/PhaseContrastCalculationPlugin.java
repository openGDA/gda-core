/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package gda.device.detector.nxdetector.plugin.areadetector.phasecontrast;

import gda.device.DeviceException;
import gda.device.detector.NXDetectorData;
import gda.device.detector.nxdata.NXDetectorDataAppender;
import gda.device.detector.nxdata.NXDetectorDataDoubleAppender;
import gda.device.detector.nxdetector.plugin.NullNXPlugin;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Vector;

public class PhaseContrastCalculationPlugin extends NullNXPlugin {

	private String roi1Name = "quadrant1";

	private String roi2Name = "quadrant2";

	private String roi3Name = "quadrant3";

	private String roi4Name = "quadrant4";

	@Override
	public String getName() {
		return "phase_contrast";
	}

	@Override
	public List<String> getInputStreamNames() {
		return Arrays.asList("Horizontal", "Vertical");
	}

	@Override
	public List<String> getInputStreamFormats() {
		return Arrays.asList("%f", "%f");
	}

	@Override
	public List<NXDetectorDataAppender> read(int maxToRead) throws NoSuchElementException, InterruptedException,
			DeviceException {
		// This does not care which point in the scan we are at.
		return Arrays.asList((NXDetectorDataAppender) new PhaseContrastCalculationAppender());
	}

	class PhaseContrastCalculationAppender implements NXDetectorDataAppender {

		@Override
		public void appendTo(NXDetectorData data, String detectorName) throws DeviceException {

			// Read the relevant stats added by the four roi/stats pairs
			
			double quad1;
			double quad2;
			double quad3;
			double quad4;
			try {
				// we want the totals of the ROIs, so add _total to the end of each roi name
				quad1 = readValue(data, roi1Name + "_total");
				quad2 = readValue(data, roi2Name + "_total");
				quad3 = readValue(data, roi3Name + "_total");
				quad4 = readValue(data, roi4Name + "_total");
			} catch (Exception e) {
				throw new DeviceException(createErrorMessage(), e);
			}

			double total = quad1 + quad2 + quad3 + quad4;
			double top_pair = quad1 + quad2;
			double bottom_pair = quad3 + quad4;

			double left_pair = quad1 + quad3;
			double right_pair = quad2 + quad4;

			double horizontal = Math.abs((top_pair - bottom_pair) / total);
			double vertical = Math.abs((left_pair - right_pair) / total);

			List<Double> values = new Vector<Double>();
			values.add(horizontal);
			values.add(vertical);

			// Make double appender and it use it for the actual appending
			NXDetectorDataDoubleAppender doubleAppender = new NXDetectorDataDoubleAppender(getInputStreamNames(),
					values);
			doubleAppender.appendTo(data, detectorName);

		}

		private String createErrorMessage() {
			return "Exception reading out quadrant totals from NXDetectorData structure.\\nWas expecting four rois with names: "
					+ roi1Name + ", " + roi2Name + ", " + roi3Name + ", and " + roi4Name + ".";
		}

		double readValue(NXDetectorData data, String fieldName) {
			Double[] values = data.getDoubleVals();
			String[] extraNames = data.getExtraNames();
			int ourValueIndex = Arrays.asList(extraNames).indexOf(fieldName);
			return values[ourValueIndex];
		}
	}

	public String getRoi1Name() {
		return roi1Name;
	}

	public void setRoi1Name(String roi1Name) {
		this.roi1Name = roi1Name;
	}

	public String getRoi2Name() {
		return roi2Name;
	}

	public void setRoi2Name(String roi2Name) {
		this.roi2Name = roi2Name;
	}

	public String getRoi3Name() {
		return roi3Name;
	}

	public void setRoi3Name(String roi3Name) {
		this.roi3Name = roi3Name;
	}

	public String getRoi4Name() {
		return roi4Name;
	}

	public void setRoi4Name(String roi4Name) {
		this.roi4Name = roi4Name;
	}
}
