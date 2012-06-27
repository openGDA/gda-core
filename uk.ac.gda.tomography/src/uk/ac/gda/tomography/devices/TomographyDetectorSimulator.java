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

package uk.ac.gda.tomography.devices;

import gda.device.DeviceException;

import java.awt.Point;
import java.awt.Rectangle;

/**
 *
 */
public class TomographyDetectorSimulator implements ITomographyDetector {

	@Override
	public void setExposureTime(double collectionTime) throws Exception {

	}

	@Override
	public void acquireMJpeg(Double acqTime, Double acqPeriod, Double procScaleFactor, int binX, int binY)
			throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void setZoomRoiStart(Point roiStart) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void setupZoomMJpeg(Rectangle roi, Point bin) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public Integer getRoi1BinX() throws Exception {
		return 1;
	}

	@Override
	public Integer getRoi2BinX() throws Exception {
		return 1;
	}

	@Override
	public void enableFlatField() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void disableFlatField() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public String getTiffFilePath() throws Exception {
		return null;
	}

	@Override
	public String getTiffFileName() throws Exception {
		return null;
	}

	@Override
	public String getTiffFileTemplate() throws Exception {
		return null;
	}

	@Override
	public void setTiffFileNumber(int fileNumber) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public String demandRaw(Double acqTime, String demandRawFilePath, String demandRawFileName, Boolean isHdf,
			Boolean isFlatFieldCorrectionRequired, Boolean demandWhileStreaming) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String takeFlat(double expTime, int numberOfImages, String fileLocation, String fileName,
			String filePathTemplate) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getTiffImageFileName() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String takeDark(int numberOfImages, double acqTime, String fileLocation, String fileName,
			String filePathTemplate) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void abort() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void setHdfFormat(boolean b) {
		// TODO Auto-generated method stub

	}

	@Override
	public void resetFileFormat() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isHdfFormat() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void resetAll() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void resetAfterTiltToInitialValues() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void setProcScale(int factor) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void setRoi1ScalingDivisor(double divisor) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public boolean isAcquiring() throws DeviceException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setExternalTriggered(Boolean val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initDetector() {
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
	public void setupForTilt(int minY, int maxY, int minX, int maxX) throws Exception {
		// TODO Auto-generated method stub
		
	}
}
