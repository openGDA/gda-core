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

package uk.ac.gda.server.ncd.subdetector;

import java.util.Arrays;

import gda.data.nexus.extractor.NexusExtractor;
import gda.data.nexus.extractor.NexusGroupData;
import gda.device.DeviceException;
import gda.device.detector.NXDetectorData;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetUtils;
import org.eclipse.dawnsci.analysis.dataset.roi.SectorROI;
import org.nexusformat.NexusFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.Nexus;
import uk.ac.diamond.scisoft.analysis.roi.ROIProfile;

public class SectorIntegration extends ReductionDetectorBase {

	private static final Logger logger = LoggerFactory.getLogger(SectorIntegration.class);

	private SectorROI roi;
	private Double gradient, intercept, cameraLength;

	public double getCameraLength() {
		return cameraLength.doubleValue();
	}

	public void setCameraLength(double cameraLength) {
		this.cameraLength = new Double(cameraLength);
	}

	public double[] getCalibrationData() {
		if (gradient != null && intercept != null)
			return new double[] {gradient.doubleValue(), intercept.doubleValue()};
		return null;
	}

	public void setCalibrationData(double gradient, double intercept) {
		this.gradient = new Double(gradient);
		this.intercept =  new Double(intercept);
	}

	@SuppressWarnings("hiding")
	private Dataset mask;
	
	public SectorIntegration(String name, String key) {
		super(name, key);
	}

	public void setMask(IDataset mask) {
		this.mask = DatasetUtils.convertToDataset(mask);
	}

	@Override
	public Dataset getMask() {
		return mask;
	}

	public void setROI(SectorROI ds) {
		roi = ds;
	}

	public SectorROI getROI() {
		return roi;
	}

	@Override
	public void writeout(int frames, NXDetectorData nxdata) throws DeviceException {
		if (roi == null) {
			return;
		}

		Dataset maskUsed = mask;

		roi.setClippingCompensation(true);

		try {
			Dataset myazdata = null, myraddata = null;

			NexusGroupData parentngd = nxdata.getData(key, "data", NexusExtractor.SDSClassName);
			Dataset parentdata = Nexus.createDataset(parentngd, false);

			uk.ac.diamond.scisoft.ncd.core.SectorIntegration sec = new uk.ac.diamond.scisoft.ncd.core.SectorIntegration();
			sec.setROI(roi);
			sec.setFast(true);
			sec.setAreaData(ROIProfile.area(Arrays.copyOfRange(parentdata.getShape(),1,3), parentdata.getDtype(), mask, roi, true, true, true));
			Dataset[] mydata = sec.process(parentdata, frames, maskUsed);
			myazdata = mydata[0];
			myraddata = mydata[1];

			NexusGroupData myazngd = Nexus.createNexusGroupData(myazdata);
			myazngd.isDetectorEntryData = true;
			nxdata.addData(getName(), "azimuth", myazngd, "1",  null);

			NexusGroupData myradngd = Nexus.createNexusGroupData(myraddata);
			myradngd.isDetectorEntryData = true;
			nxdata.addData(getName(), myradngd, "1", 1);

			NexusGroupData roiData = new NexusGroupData(new int[] { 2 }, NexusFile.NX_FLOAT64, roi.getPoint());
			roiData.isDetectorEntryData = false;
			nxdata.addData(getName(), "beam centre", roiData, "pixels", 0);
			roiData = new NexusGroupData(new int[] { 2 }, NexusFile.NX_FLOAT64, roi.getAnglesDegrees());
			roiData.isDetectorEntryData = false;
			nxdata.addData(getName(), "integration angles", roiData, "Deg", 0);
			roiData = new NexusGroupData(new int[] { 2 }, NexusFile.NX_FLOAT64, roi.getRadii());
			roiData.isDetectorEntryData = false;
			nxdata.addData(getName(), "integration radii", roiData, "pixels", 0);
			nxdata.addData(getName(), "integration symmetry", new NexusGroupData(roi.getSymmetryText()), null, 0);
			if (gradient != null && intercept != null) {
				double[] calibrationValues =  new double[] {gradient.doubleValue(), intercept.doubleValue()};
				NexusGroupData calibrationData = new NexusGroupData(new int[] { 2 }, NexusFile.NX_FLOAT64, calibrationValues);
				calibrationData.isDetectorEntryData = false;
				nxdata.addData(getName(), "qaxis calibration", calibrationData, null, 0);
			}
			if (cameraLength != null) {
				NexusGroupData cameraData = new NexusGroupData(new int[] { 1 }, NexusFile.NX_FLOAT64, new double[] {cameraLength.doubleValue()});
				cameraData.isDetectorEntryData = false;
				nxdata.addData(getName(), "camera length", cameraData, "mm", 0);
			}
			if (maskUsed != null) {
				nxdata.addData(getName(), "mask", Nexus.createNexusGroupData(maskUsed), "pixel", 0);
			}
			addQAxis(nxdata, parentngd.dimensions.length - 1);

			addMetadata(nxdata);
		} catch (Exception e) {
			logger.error("exception caught reducing data", e);
		}
	}
}