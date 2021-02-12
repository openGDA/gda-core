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

package uk.ac.gda.client.properties.camera;

import uk.ac.gda.api.camera.ImageMode;

/**
 * Defines the properties which allows a camera to start acquiring.
 *
 * <p>
 * These properties allows the GUI to start a stream in order the user can visualise, live, the camera acquisitions
 * </p>
 */
public class StreamConfiguration {

	/**
	 * Indicates where this camera should be monitored. If {@code true}, the GUI code is expected to display the camera status
	 */
	private boolean active;

	/**
	 * The camera {@link ImageMode} to use for start the camera acquistion
	 */
	private ImageMode imageMode;

	/**
	 * The camera trigger to use for start the camera acquistion.
	 *
	 * <p>
	 * This property is completely dependent on the specific camera control implementation.
	 * For this reason to know the appropriate value look at the CSS client.
	 * </p>
	 */
	private short triggerMode;

	/**
	 * @return the camera start image mode
	 */
	public ImageMode getImageMode() {
		return imageMode;
	}

	public void setImageMode(ImageMode imageMode) {
		this.imageMode = imageMode;
	}

	/**
	 * The camera trigger to use for start the camera acquistion.
	 *
	 * <p>
	 * This property is typically used from GDA to set in the trigger mode directly in the camera.
	 *
	 * This property is completely dependent on the specific camera control implementation.
	 * For this reason to know the appropriate value look at the CSS client.
	 * </p>
	 *
	 * @return the camera start trigger mode
	 */
	public short getTriggerMode() {
		return triggerMode;
	}

	public void setTriggerMode(short triggerMode) {
		this.triggerMode = triggerMode;
	}

	/**
	 * Indicates where this camera should be monitored. If {@code true}, the GUI code is expected to display the camera status
	 * @return {@code true} do monitor, {@code false} do not monitor.
	 */
	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
}
