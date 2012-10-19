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

package uk.ac.gda.client.tomo.composites;

import java.lang.reflect.InvocationTargetException;

import uk.ac.gda.client.tomo.composites.TomoAlignmentLeftPanelComposite.SAMPLE_OR_FLAT;
import uk.ac.gda.client.tomo.composites.ZoomButtonComposite.ZoomButtonActionListener;

public interface ITomoAlignmentLeftPanelListener extends ZoomButtonActionListener {

	/**
	 * When the sample stream button is called
	 * 
	 * @param selected
	 * @throws Exception
	 */
	void stream(boolean selected) throws Exception;

	/**
	 * when the sample single button is called
	 * 
	 * @param isFlatCorrectionRequired
	 * @throws InvocationTargetException
	 * @throws Exception
	 */
	void single(boolean isFlatCorrectionRequired) throws InvocationTargetException, Exception;

	/**
	 * fast preview is requested.
	 * 
	 * @param isFlatCorrectionRequired
	 * @throws InvocationTargetException
	 * @throws Exception
	 */
	void fastPreview(boolean isFlatCorrectionRequired) throws InvocationTargetException, Exception;

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
	 * Informs listeners that the sample histogram has been selected.
	 * 
	 * @param selection
	 * @throws Exception
	 */
	void histogram(boolean selection) throws Exception;

	/**
	 * Informs the listeners that "Sample In" has been requested.
	 * 
	 * @throws InterruptedException
	 * @throws InvocationTargetException
	 */
	void moveSampleIn() throws InvocationTargetException, InterruptedException;

	/**
	 * Informs the listeners that "Sample Out" has been requested.
	 * 
	 * @throws InterruptedException
	 * @throws InvocationTargetException
	 */

	void moveSampleOut() throws InvocationTargetException, InterruptedException;

	/**
	 * request to show cross hair
	 * 
	 * @param selection
	 * @throws Exception
	 */
	void crosshair(boolean selection) throws Exception;

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
	 * Event published when the exposure time mode is changed.
	 * 
	 * @param sample
	 * @throws Exception
	 */
	void exposureStateChanged(SAMPLE_OR_FLAT sample) throws Exception;

}
