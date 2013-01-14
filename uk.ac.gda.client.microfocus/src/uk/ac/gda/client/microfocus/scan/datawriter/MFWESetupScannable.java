/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package uk.ac.gda.client.microfocus.scan.datawriter;

import gda.device.Detector;
import gda.device.scannable.ScanDataListenerScannable;
import gda.scan.IScanDataPoint;

/**
* Scannable to use MicrofocusWriterExtender 
*/
public class MFWESetupScannable extends ScanDataListenerScannable {
	private MicroFocusWriterExtender mfd = null;
	private String detectorBeanFileName = "";
	private Detector[] detectors;
	private String[] roiNames;
	private String initialSelectedElement;

	public String getInitialSelectedElement() {
		return initialSelectedElement;
	}

	public void setInitialSelectedElement(String initialSelectedElement) {
		this.initialSelectedElement = initialSelectedElement;
	}

	public String[] getRoiNames() {
		return roiNames;
	}

	public void setRoiNames(String[] roiNames) {
		this.roiNames = roiNames;
	}

	public Detector[] getDetectors() {
		return detectors;
	}

	public void setDetectors(Detector[] detectors) {
		this.detectors = detectors;
	}

	public MFWESetupScannable() {
		setName("MFWESetupScannable");
	}

	public String getDetectorBeanFileName() {
		return detectorBeanFileName;
	}

	public void setDetectorBeanFileName(String detectorBeanFileName) {
		this.detectorBeanFileName = detectorBeanFileName;
	}

	@Override
	public void handleScanDataPoint(IScanDataPoint sdp) throws Exception {
		if (sdp.getCurrentPointNumber() == 0) {
			mfd = null;
			mfd = createMFD(sdp);
			// InterfaceProvider.getJythonNamespace().placeInJythonNamespace("microfocusScanWriter", mfd);
		}
		if (mfd != null) {
			mfd.addData(null, sdp);
		}
	}

	private MicroFocusWriterExtender createMFD(IScanDataPoint sdp) {
		int[] dims = getScanDimensions(sdp);
		if (dims.length != 2)
			return null;
		// ok a candidate
		MicroFocusWriterExtender ext = new MicroFocusWriterExtender(dims[0], dims[1], 1, 1);
		ext.setPlotName("MapPlot");
		ext.setDetectorBeanFileName(detectorBeanFileName);
		ext.setDetectors(detectors);
		ext.getWindowsfromBean();
//		ext.setRoiFromBean();//commented due to merge issue from 8.22. This method can go back in but I (Chris C) first want to see what this new class is for. (Ask Paul G)
//		ext.setSelectedElement(initialSelectedElement);
		ext.setEnergyValue(1.0);
		ext.setZValue(0.);
		return ext;
	}

	@Override
	public void handleScanEnd() {
	}
}
