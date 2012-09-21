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

package uk.ac.gda.client.tomo.configuration.viewer;

import uk.ac.gda.client.tomo.composites.CameraControlComposite;
import uk.ac.gda.client.tomo.composites.MotionControlComposite.SAMPLE_WEIGHT;
import uk.ac.gda.tomography.parameters.AlignmentConfiguration;
import uk.ac.gda.tomography.parameters.Resolution;
import uk.ac.gda.tomography.parameters.SampleWeight;

public class TomoConfigViewerUtil {
	/**
	 * XXX - Need to move this into a configurable value
	 */
	private static final String T3_M1Z = "t3_m1z";

	public static void setupConfigContent(AlignmentConfiguration alignmentConfiguration, TomoConfigContent configContent) {
		configContent.setSelectedToRun(alignmentConfiguration.getSelectedToRun());
		configContent.setSampleExposureTime(alignmentConfiguration.getSampleExposureTime());
		configContent.setFlatExposureTime(alignmentConfiguration.getFlatExposureTime());
		configContent.setConfigId(alignmentConfiguration.getId());
		configContent.setSampleDescription(alignmentConfiguration.getDescription());
		configContent.setProposalId(alignmentConfiguration.getProposalId());
		configContent.setModuleNumber(alignmentConfiguration.getDetectorProperties().getModuleParameters()
				.getModuleNumber());
		configContent.setEnergy(alignmentConfiguration.getEnergy());
		configContent.setSampleDetectorDistance(alignmentConfiguration.getMotorPosition(T3_M1Z));

		configContent.setSampleWeight(getSampleWeightString(alignmentConfiguration.getSampleWeight()));
		configContent.setResolution(getResolutionString(alignmentConfiguration.getDetectorProperties()
				.getDesired3DResolution()));
		configContent.setTimeDivider(Integer.toString(alignmentConfiguration.getDetectorProperties()
				.getAcquisitionTimeDivider()));
		configContent.setFramesPerProjection(alignmentConfiguration.getDetectorProperties()
				.getNumberOfFramerPerProjection());
		configContent.setScanMode(alignmentConfiguration.getScanMode().toString());
		configContent.setModuleObjectPixelSize(alignmentConfiguration.getDetectorProperties().getModuleParameters()
				.getHorizontalFieldOfView());
	}

	private static String getResolutionString(Resolution desired3dResolution) {
		switch (desired3dResolution) {
		case FULL:
			return CameraControlComposite.RESOLUTION.FULL.toString();
		case X2:
			return CameraControlComposite.RESOLUTION.TWO_X.toString();
		case X4:
			return CameraControlComposite.RESOLUTION.FOUR_X.toString();
		case X8:
			return CameraControlComposite.RESOLUTION.EIGHT_X.toString();
		}
		return null;
	}

	private static String getSampleWeightString(SampleWeight sampleWeight) {
		switch (sampleWeight) {
		case LESS_THAN_1:
			return SAMPLE_WEIGHT.LESS_THAN_ONE.toString();
		case ONE_TO_TEN:
			return SAMPLE_WEIGHT.ONE_TO_TEN.toString();
		case TEN_TO_TWENTY:
			return SAMPLE_WEIGHT.TEN_TO_TWENTY.toString();
		case TWENTY_TO_FIFTY:
			return SAMPLE_WEIGHT.TWENTY_TO_FIFTY.toString();
		}
		return null;
	}

}
