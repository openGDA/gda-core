/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package uk.ac.gda.client.tomo.alignment.view.handlers.simulator;

import gda.device.DeviceException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.Point;

import uk.ac.gda.client.tomo.TiffFileInfo;
import uk.ac.gda.client.tomo.alignment.view.controller.TomoAlignmentController;
import uk.ac.gda.client.tomo.alignment.view.handlers.ICameraHandler;
import uk.ac.gda.client.tomo.composites.ZoomButtonComposite.ZOOM_LEVEL;

/**
 *
 */
public class TomoDetectorHandlerSimulator implements ICameraHandler {

	@Override
	public void setExposureTime(double exposureTime) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void startAcquiring(double acqTime, int amplifierValue) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void stopAcquiring() throws DeviceException, Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void takeFlat(IProgressMonitor monitor, int numFlat, double expTime) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public String demandRaw(IProgressMonitor monitor, double acqTime, boolean isFlatFieldRequired) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String demandRawWithStreamOn(IProgressMonitor monitor, boolean flatCorrectionSelected) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setZoomRoiLocation(Point roiStart) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void setupZoom(ZOOM_LEVEL zoomLevel) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public double getAcqExposureRBV() throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getAcqPeriodRBV() throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getAcquireState() throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getArrayCounter_RBV() throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getArrayRate_RBV() throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getNumExposuresCounter_RBV() throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getNumImagesCounter_RBV() throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getTimeRemaining_RBV() throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getDatatype() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getFullMJpegURL() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getZoomImgMJPegURL() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setViewController(TomoAlignmentController tomoAlignmentViewController) {
		// TODO Auto-generated method stub

	}

	@Override
	public Double getDetectorPixelSize() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer getRoi1BinX() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getTiffFullFileName() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void enableFlatCorrection() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void disableFlatCorrection() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public String getFlatImageFullFileName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void takeDark(IProgressMonitor monitor, double acqTime) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public Boolean getProc1FlatFieldCorrection() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getTakeFlatNumImages() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getPreferredSampleExposureTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Integer getSaturationThreshold() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getStatMin() throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getStatMax() throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getStatMean() throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double getStatSigma() throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub

	}

	@Override
	public Integer getRoi2BinX() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setAmplifiedValue(double newExpTime, int factor) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void stopDemandRaw() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public String getCameraName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public short[] getArrayData() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void resetFileFormat() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public TiffFileInfo getTiffFileInfo() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setTiffFileNumber(int fileNumber) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void reset() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public int getRoi1BinValue() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getDarkImageFullFileName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void resetAfterTilt() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPreferredSampleExposureTime(double exposureTime) {
		// TODO Auto-generated method stub

	}

	@Override
	public double getPreferredFlatExposureTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setPreferredFlatExposureTime(double exposureTime) {
		// TODO Auto-generated method stub

	}

	@Override
	public void disableDarkSubtraction() {
		// TODO Auto-generated method stub

	}

	@Override
	public void enableDarkSubtraction() {
		// TODO Auto-generated method stub

	}

	@Override
	public String getDarkFieldImageFullFileName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setUpForTilt(int minY, int maxY, int minX, int maxX) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public int getFullImageWidth() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getFullImageHeight() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setProc1ScaleValue(double scaledValue) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public double getProc1Scale() throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void applyScalingAndContrast(double offset, double scale) throws Exception {
		// TODO Auto-generated method stub

	}

}
