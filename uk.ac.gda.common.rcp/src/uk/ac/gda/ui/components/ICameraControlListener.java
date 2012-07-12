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
package uk.ac.gda.ui.components;

import java.lang.reflect.InvocationTargetException;

import uk.ac.gda.ui.components.ZoomButtonComposite.ZoomButtonActionListener;

/**
 * Interface to provide listeners of the changes on the {@link CameraControlComposite}
 */
public interface ICameraControlListener extends ZoomButtonActionListener {
	/**
	 * When the sample stream button is called
	 * 
	 * @param selected
	 * @throws Exception
	 */
	void sampleStream(boolean selected) throws Exception;

	/**
	 * when the sample single button is called
	 * 
	 * @param isFlatCorrectionRequired
	 * @throws InvocationTargetException
	 * @throws Exception
	 */
	void sampleSingle(boolean isFlatCorrectionRequired) throws InvocationTargetException, Exception;

	/**
	 * When the Flat Single button is pressed
	 * 
	 * @param isFlatCorrectionRequired
	 * @throws InvocationTargetException
	 * @throws Exception
	 */
	void flatSingle(boolean isFlatCorrectionRequired) throws InvocationTargetException, Exception;

	/**
	 * When the Flat Stream button is pressed
	 * 
	 * @param selected
	 * @throws Exception
	 */
	void flatStream(boolean selected) throws Exception;

	/**
	 * When the exposure time for the sample is changed.
	 * 
	 * @param sampleExposureTime
	 * @throws Exception
	 */
	void sampleExposureTimeChanged(double sampleExposureTime) throws Exception;

	/**
	 * When the exposure time for the flat is changed
	 * 
	 * @param flatExposureTime
	 * @throws Exception
	 */
	void flatExposureTimeChanged(double flatExposureTime) throws Exception;

	/**
	 * When the saturation button is pressed
	 * 
	 * @param selected
	 * @throws IllegalArgumentException
	 */
	void saturation(boolean selected) throws IllegalArgumentException;

	/**
	 * When the take flat button is pressed.
	 * 
	 * @throws InterruptedException
	 * @throws InvocationTargetException
	 */
	void takeFlatAndDark() throws InterruptedException, InvocationTargetException;

	/**
	 * When the Correct flat button is pressed.
	 * 
	 * @param selected
	 * @throws Exception
	 */
	void correctFlatAndDark(boolean selected) throws Exception;

	/**
	 * When the show flat button is pressed.
	 * 
	 * @throws Exception
	 */
	void showFlat() throws Exception;

	/**
	 * When the show dark button is pressed.
	 * 
	 * @throws Exception
	 */
	void showDark() throws Exception;

	/**
	 * When the profile button is pressed.
	 * 
	 * @param selected
	 * @throws Exception
	 */
	void profile(boolean selected) throws Exception;

	/**
	 * When the exposure time for the sample is set
	 * 
	 * @param parseDouble
	 * @throws Exception
	 */
	// void setExposureTime(double parseDouble) throws Exception;

	/**
	 * Event fired to update the preferred sample exposure time.
	 * 
	 * @throws Exception
	 */
	void updatePreferredSampleExposureTime() throws Exception;

	/**
	 * * Event fired to update the preferred sample exposure time.
	 * 
	 * @throws Exception
	 */
	void updatePreferredFlatExposureTime() throws Exception;

	/**
	 * @throws Exception
	 */
	void sampleFlatTimeChanged() throws Exception;

	/**
	 * Save alignment configuration
	 * @throws InterruptedException 
	 * @throws InvocationTargetException 
	 * @throws Exception 
	 */
	void saveAlignmentConfiguration() throws InvocationTargetException, InterruptedException, Exception;

	/**
	 * Informs the listeners that the sample description has changed.
	 * 
	 * @param sampleDescription
	 */
	void sampleDescriptionChanged(String sampleDescription);

	/**
	 * Informs listeners that the sample histogram has been selected.
	 * 
	 * @param selection
	 * @throws Exception
	 */
	void sampleHistogram(boolean selection) throws Exception;

	/**
	 * Informs listeners that the flat histogram has been selected.
	 * 
	 * @param selection
	 * @throws Exception
	 */
	void flatHistogram(boolean selection) throws Exception;

	/**
	 * Informs the listeners that "Define ROI" button has been selected.
	 * 
	 * @param selection
	 */
	void defineRoi(boolean selection);

	/**
	 * Informs the listeners to reset the ROI bounds
	 */
	void resetRoi();

	/**
	 * Informs the listeners that "Sample In" has been requested.
	 * @throws InterruptedException 
	 * @throws InvocationTargetException 
	 */
	void moveSampleIn() throws InvocationTargetException, InterruptedException;

	/**
	 * Informs the listeners that "Sample Out" has been requested.
	 * @throws InterruptedException 
	 * @throws InvocationTargetException 
	 */

	void moveSampleOut() throws InvocationTargetException, InterruptedException;

}