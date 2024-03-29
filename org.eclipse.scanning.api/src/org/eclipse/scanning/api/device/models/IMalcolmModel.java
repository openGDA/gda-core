/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.api.device.models;

import java.util.List;

/**
 * Model for a malcolm device.
 */
public interface IMalcolmModel extends IDetectorModel {

	/**
	 * Get the names of the scan axes that are controlled by malcolm.
	 * @return axes to move
	 */
	public List<String> getAxesToMove();

	public void setAxesToMove(List<String> axesToMove);

	/**
	 * Get the models for the detectors controlled by malcolm.
	 * @return malcolm detector models
	 */
	public List<IMalcolmDetectorModel> getDetectorModels();

	public void setDetectorModels(List<IMalcolmDetectorModel> detectorModels);

	/**
	 * Get the minimum exposure time for the current malcolm model
	 * @return exposure time for current detector
	 */
	public double getMinExposureTime();

	public void setMinExposureTime(double minExposureTime);

}
