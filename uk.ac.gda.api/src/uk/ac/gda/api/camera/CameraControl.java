/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package uk.ac.gda.api.camera;

import gda.device.DeviceException;
import gda.factory.Findable;
import gda.observable.IObservable;

/**
 * An interface allowing access to basic camera operations
 * <p>
 * It was originally written to allow clients to control camera functions without having to export large objects such as
 * area detectors over RMI.
 */
public interface CameraControl extends Findable, IObservable {

	/**
	 * Get acquire (exposure) time
	 *
	 * @return Acquire time in seconds
	 * @throws DeviceException
	 */
	double getAcquireTime() throws DeviceException;

	/**
	 * Set acquire (exposure) time
	 *
	 * @param acquiretime
	 *            in seconds
	 * @throws DeviceException
	 */
	void setAcquireTime(double acquiretime) throws DeviceException;

	/**
	 * Start acquiring data
	 *
	 * @throws DeviceException
	 */
	void startAcquiring() throws DeviceException;

	/**
	 * Stop acquiring data
	 *
	 * @throws DeviceException
	 */
	void stopAcquiring() throws DeviceException;

	/**
	 * Get the camera acquire state
	 * <p>
	 * @return acquire state
	 * @throws DeviceException
	 */
	CameraState getAcquireState() throws DeviceException;

	/**
	 * Return binning format of the camera e.g 1x1
	 *
	 * @return binning format
	 */
	BinningFormat getBinningPixels () throws DeviceException;

	/**
	 * The amount of
	 * @param binningFormat
	 */
	void setBinningPixels (BinningFormat binningFormat) throws DeviceException;

	/**
	 *
	 * @return frame size
	 * @throws DeviceException
	 */
	int[] getFrameSize () throws DeviceException;

	/**
	 *
	 * @return array of left, top, width, height
	 * @throws DeviceException
	 */
	int[] getRoi () throws DeviceException;

	/**
	 *
	 * @param left
	 * @param top
	 * @param width
	 * @param height
	 * @throws DeviceException
	 */
	void setRoi (int left, int top, int width, int height) throws DeviceException;

	/**
	 *
	 * @throws DeviceException
	 */
	void clearRoi () throws DeviceException;

	/**
	 * Gets the X-coordinate of the overlay
	 *
	 * @return The X-coordinate of the overlay
	 */
	int getOverlayCentreX() throws DeviceException;

	/**
	 * Gets the Y-coordinate of the overlay
	 *
	 * @return The Y-coordinate of the overlay
	 */
	int getOverlayCentreY() throws DeviceException;

	/**
	 * Sets the {@link ImageMode} of the camera
	 *
	 * @param imageMode
	 * @throws Exception
	 */
	void setImageMode(ImageMode imageMode) throws Exception;

	/**
	 * Gets the {@link ImageMode} of the camera
	 * @throws Exception
	 */
	ImageMode getImageMode() throws Exception;

	/**
	 * Sets the camera trigger mode
	 *
	 * @param triggerMode the camera specific trigger mode value
	 * @throws Exception
	 */
	void setTriggerMode(short triggerMode) throws Exception;

	/**
	 * Gets the camera trigger mode
	 * @throws Exception
	 */
	short getTriggerMode() throws Exception;

	/**
	 * Enables the processing filter
	 */
	void enableProcessingFilter() throws Exception;

	/**
	 * Disables the processing filter
	 */
	void disableProcessingFilter() throws Exception;

	/**
	 * Selects the type of recursive filter to be used by the processing plugin
	 *
	 * @throws Exception
	 */
	void setProcessingFilterType(int filterType) throws Exception;

	/**
	 * Resets the filter
	 */
	void resetFilter() throws Exception;

	/**
	 * Gets the X dimension of image
	 */
	public int getImageSizeX() throws DeviceException;

	/**
	 *
	 * Gets the Y dimension of image
	 */
	public int getImageSizeY() throws DeviceException;

	/**
	 *  Sets the number of images
	 */
	void setNumImages(int numImages) throws Exception;

	/**
	 * Gets the number of images
	 */
	public int getNumImages() throws Exception;


}
