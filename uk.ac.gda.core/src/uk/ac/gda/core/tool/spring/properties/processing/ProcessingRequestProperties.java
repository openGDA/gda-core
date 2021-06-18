/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.gda.core.tool.spring.properties.processing;

import uk.ac.diamond.daq.scanning.CalibrationFrameCollector;
import uk.ac.gda.api.acquisition.configuration.processing.FrameCaptureRequest;

/**
 * Contains properties to configure the processing requests submitted by the client.
 *
 * @author Maurizio Nagni
 *
 */
public class ProcessingRequestProperties {

	private String frameCaptureDecorator;

	private DiffractionCalibrationMerge diffractionCalibrationMerge;

	/**
	 * @return the bean id of {@link CalibrationFrameCollector} instance
	 *
	 * @see FrameCaptureRequest
	 */
	public String getFrameCaptureDecorator() {
		return frameCaptureDecorator;
	}

	public void setFrameCaptureDecorator(String frameCaptureDecorator) {
		this.frameCaptureDecorator = frameCaptureDecorator;
	}

	public DiffractionCalibrationMerge getDiffractionCalibrationMerge() {
		return diffractionCalibrationMerge;
	}

	public void setDiffractionCalibrationMerge(DiffractionCalibrationMerge diffractionCalibrationMerge) {
		this.diffractionCalibrationMerge = diffractionCalibrationMerge;
	}
}
