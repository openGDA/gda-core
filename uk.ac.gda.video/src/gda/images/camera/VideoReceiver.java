/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.images.camera;

import gda.device.DeviceException;
import gda.factory.Configurable;

/**
 * Interface to be implemented by objects that can receive video streams and send the frames to any number of
 * {@link ImageListener}s.
 */
public interface VideoReceiver<E> extends Configurable {
	
	/**
	 * Adds a listener to which captured frames will be passed.
	 */
	public void addImageListener(ImageListener<E> listener);
	
	/**
	 * Removes a listener from the list of listeners to which captured frames will be passed.
	 */
	public void removeImageListener(ImageListener<E> listener);
	
	/**
	 * Connects to the video stream and starts dispatching images to listeners.
	 */
	public void createConnection();
	
	/**
	 * Stops frame capture.
	 */
	public void stop();
	
	/**
	 * Starts frame capture.
	 */
	public void start();
	
	/**
	 * Grabs a frame from the video stream.
	 */
	public E getImage() throws DeviceException;
	
	/**
	 * Disconnects from the video stream.
	 */
	public void closeConnection();
	
	
	/**
	 * Returns the video source's display name. This is a label used in the UI
	 * to distinguish between multiple video sources.
	 * 
	 * @return display name
	 */
	public String getDisplayName();
}
