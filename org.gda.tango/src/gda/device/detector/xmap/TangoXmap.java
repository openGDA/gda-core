/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package gda.device.detector.xmap;

import gda.device.DeviceException;
import gda.device.Timer;
import gda.jython.InterfaceProvider;
import gda.scan.ScanInformation;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.beans.DetectorROI;
import uk.ac.gda.beans.vortex.DetectorElement;
import uk.ac.gda.beans.vortex.VortexParameters;
import uk.ac.gda.devices.detector.FluorescenceDetector;
import uk.ac.gda.devices.detector.FluorescenceDetectorParameters;

public class TangoXmap extends Xmap implements FluorescenceDetector {

	private static final Logger logger = LoggerFactory.getLogger(TangoXmap.class);

	@Override
	protected void configureChannelLabels(VortexParameters vp) {
		channelLabels = new ArrayList<String>();
		ArrayList<String> outputFormat = new ArrayList<String>();
		int roiNum = 0;
		for (DetectorROI roi : vp.getDetectorList().get(0).getRegionList()) {
			String name = roi.getRoiName();
			if (name == null)
				name = "ROI " + roiNum;
			logger.debug("Setting channel label for rois: " + name);

			channelLabels.add(name);
			outputFormat.add("%9d");

			++roiNum;
		}
		int nelem = getNumberOfElements();
		for (int i = 0; i < nelem; i++) {
			// add in additional names
			channelLabels.add("events" + i);
			channelLabels.add("ICR" + i);
			channelLabels.add("OCR" + i);
			channelLabels.add("LiveTime" + i);
			channelLabels.add("DeadTime%" + i);
			// add the same number of additional formats
			outputFormat.add("%5.5g");
			outputFormat.add("%5.5g");
			outputFormat.add("%5.5g");
			outputFormat.add("%5.5g");
			outputFormat.add("%5.5g");
		}
		channelLabels.add("FF");
		outputFormat.add("%5.5g");
		setExtraNames(channelLabels.toArray(new String[0]));
		setOutputFormat(outputFormat.toArray(new String[0]));
	}

	/**
	 * Works for different regions in each element.
	 *
	 * @param vp
	 * @throws Exception
	 */
	@Override
	protected void configureRegionsOfInterest(final VortexParameters vp) throws Exception {

		try {
			final List<DetectorElement> dl = vp.getDetectorList();
			ArrayList<Integer> rois = new ArrayList<Integer>();
			int elementIndex = 0;
			for (DetectorElement e : dl) {
				final List<DetectorROI> regions = e.getRegionList();
				for (DetectorROI roi : regions) {
					rois.add(elementIndex);
					rois.add(roi.getRoiStart());
					rois.add(roi.getRoiEnd());
				}
				++elementIndex;
			}
			int[] roiList = new int[rois.size()];
			for (int i = 0; i < rois.size(); i++) {
				roiList[i] = rois.get(i);
			}
			if (((TangoXmapController) controller).isConfigured()) {
				((TangoXmapController) controller).setAllROIs(roiList);
			}
		} catch (DeviceException e) {
			logger.error(e.getMessage(), e);
			throw new Exception("Cannot configure vortex regions of interest.", e);
		}
	}

	@Override
	public int getNumberOfElements() {
		int nelements = 0;
		try {
			nelements = controller.getNumberOfElements();
		} catch (DeviceException e) {
			logger.error("Cannot get the number of elements", e);
		}
		return nelements;
	}

	public Timer getTimer() {
		return tfg;
	}

	public void setTimer(Timer tfg) {
		this.tfg = tfg;
	}

	@Override
	public double[][] getMCAData(double time) throws DeviceException {
		return ((TangoXmapController) controller).getMCAData(time);
	}

	@Override
	public int getMCASize() {
		return ((TangoXmapController) controller).getMCASize();
	}

	@Override
	public void applyConfigurationParameters(FluorescenceDetectorParameters parameters) throws Exception {
		configureRegionsOfInterest(vortexParameters);
		configureChannelLabels(vortexParameters);
	}

	@Override
	public void atScanStart() throws DeviceException {
		super.atScanStart();
		ScanInformation currentscan = InterfaceProvider.getCurrentScanInformationHolder().getCurrentScanInformation();
		int currentScanNumber = currentscan.getScanNumber();
		((TangoXmapController) controller).setFileIndex(currentScanNumber);
	}

	public void setFilePath(String filePath) {
		// Don't overwrite the controller filepath in case the device server runs on a doze box
		// unless our directory is remotely mounted.
		if (((TangoXmapController) controller).getFilePath() == null) {
			((TangoXmapController) controller).setFilePath(filePath);
		}
	}

	@Override
	public FluorescenceDetectorParameters getConfigurationParameters() {
		return vortexParameters;
	}

	@Override
	public int getMaxNumberOfRois() {
		return ((TangoXmapController) controller).getMaxScas();
	}

	@Override
	public Object readout() throws DeviceException {
		int nelem = getNumberOfElements();
		double[] stats = ((TangoXmapController) controller).readStats();
		double[] additionalData = new double[nelem * 5 + 1];
		int j = 0;
		for (int i = 0; i < nelem; i++) {
			additionalData[j++] = stats[i * 5 + 1];
			additionalData[j++] = stats[i * 5 + 2];
			additionalData[j++] = stats[i * 5 + 3];
			additionalData[j++] = stats[i * 5 + 4];
			additionalData[j++] = stats[i * 5 + 5];
		}
		additionalData[j] = getFF();
		return ArrayUtils.addAll(((TangoXmapController) controller).getROIsSum(), additionalData);
	}

	public double getFF() throws DeviceException {
		double[] rois = ((TangoXmapController) controller).getROIsSum();
		double sum = 0.0;
		for (int i = 0; i < rois.length; i++) {
			sum += rois[i];
		}
		return sum;
	}

	@Override
	public double readoutScalerData() throws DeviceException {
		// This, IMHO, is a wrongly named method. Call the correctly named method!
		return getFF();
	}
}
