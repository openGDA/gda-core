/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.client.gui.camera.event;

/**
 *  Describes a camera exposure change event.
 *
 * @author Maurizio Nagni
 */
public class ExposureChangeEvent extends CameraEvent {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -454684975840133320L;
	private final double exposureTime;

    public ExposureChangeEvent(Object source, double exposureTime) {
		super(source);
		this.exposureTime = exposureTime;
	}

	public double getExposureTime() {
		return exposureTime;
	}
}
