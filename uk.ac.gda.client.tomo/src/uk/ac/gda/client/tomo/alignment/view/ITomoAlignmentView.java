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

package uk.ac.gda.client.tomo.alignment.view;

import java.util.Map;

import uk.ac.gda.client.tomo.StatInfo;
import uk.ac.gda.client.tomo.alignment.view.controller.TomoAlignmentController.SAMPLE_STAGE_STATE;
import uk.ac.gda.client.tomo.composites.ModuleButtonComposite.CAMERA_MODULE;
import uk.ac.gda.client.tomo.composites.TomoAlignmentControlComposite.RESOLUTION;

/**
 * Interface by which the tomo alignment controller communicates with the tomo alignment view.
 */
public interface ITomoAlignmentView extends IRotationMotorListener {

	void updateFullImgStreamUrl(String mjPegURL);

	void updateStreamWidget(int acquisitionState);

	void updateExposureTimeToWidget(double acqExposure);

	void updateZoomImgStreamUrl(String zoomImgJpegURL);

	Double getScreenPixelSize();

	void updateLeftWindowNumPixelsLabel(String cameraScaleBarDisplayText, int barLengthInPixel);

	void setFlatFieldCorrection(boolean enabled);

	void setPreferredSampleExposureTimeToWidget(double preferredExposureTime);

	void setPreferredFlatExposureTimeToWidget(double preferredExposureTime);

	// void stopStreaming();

	void setCameraModule(CAMERA_MODULE module);

	void updateStatInfo(StatInfo statInfo, String val);

	void updateRightWindowNumPixelsLabel(String cameraScaleBarDisplayText, int barLengthInPixel);

	void updateErrorAligningTilt(String status);

	void reset();

	void updateModuleButtonText(String unitsToBeDisplayed, Map<Integer, String> moduleButtonText);

	void setCameraMotionMotorPosition(double cameraMotionMotorPosition);

	void setSampleInOutState(SAMPLE_STAGE_STATE state);

	void setEnergy(double energy);

	void setResolutionPixelSize(String resolutionPixelSize);

	void setResolution(RESOLUTION res);

	void setAdjustedPreferredExposureTimeToWidget(double preferredExposureTime);

}
