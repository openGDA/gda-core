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

public class PhaseContrastCalculationPlugin extends NullNXPlugin {



	private String roi1StatName = "roi1_Total"; // TODO just a guess, depends on how you configure the four roi/stat pairs

	private String roi2StatName = "roi2_Total"; 
	
	private String roi3StatName = "roi3_Total"; 
	
	private String roi4StatName = "roi4_Total"; 

	@Override
	public String getName() {
		return "phase_contrast";
	}

	@Override
	public List<String> getInputStreamNames() {
		return Arrays.asList("a", "b", "c"); // TODO: -> transmission, horizontal, vertical (or similar)
	}

	@Override
	public List<String> getInputStreamFormats() {
		return Arrays.asList("%f", "%f", "f");
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
			// TODO: Throw an exception if not found stating that the four plugins are expected to be in the detectr's list
			//       of additional plugins, and that they must proceed this one. Also say that the roi1StatName - roi4StatName
			// attributes must be configured to match whatever fieldname the four plugins are writing to. It wopuld be helpful
			// to print the available fieldnames if you can work out how!
			
			double sum1 = readValue(data, detectorName, roi1StatName);
			double sum2 = readValue(data, detectorName, roi2StatName);
			double sum3 = readValue(data, detectorName, roi3StatName);
			double sum4 = readValue(data, detectorName, roi4StatName);
			
			// TODO: math here!
			double a = sum1 + sum2 +sum3 +sum4;
			double b = sum1 + sum2;
			double c = sum3 + sum4;
			
			// Make double appender and it use it for the actual appending
			NXDetectorDataDoubleAppender doubleAppender = new NXDetectorDataDoubleAppender(getInputStreamNames(), null);
			doubleAppender.appendTo(data, detectorName);
			
		}
		
		// TODO: Won't work fisrt time. Tobias might help a bit.
		double readValue(NXDetectorData data, String detectorName, String fieldName) {
			Double[] values = data.getDoubleVals();
			String[] extraNames = data.getExtraNames();
			int ourValueIndex = Arrays.asList(extraNames).indexOf(fieldName);
			return values[ourValueIndex];
		}
	}

	////
	
	public String getRoi1StatName() {
		return roi1StatName;
	}

	public void setRoi1StatName(String roi1StatName) {
		this.roi1StatName = roi1StatName;
	}

	public String getRoi2StatName() {
		return roi2StatName;
	}

	public void setRoi2StatName(String roi2StatName) {
		this.roi2StatName = roi2StatName;
	}

	public String getRoi3StatName() {
		return roi3StatName;
	}

	public void setRoi3StatName(String roi3StatName) {
		this.roi3StatName = roi3StatName;
	}

	public String getRoi4StatName() {
		return roi4StatName;
	}

	public void setRoi4StatName(String roi4StatName) {
		this.roi4StatName = roi4StatName;
	}
}
