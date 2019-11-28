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

import org.springframework.context.ApplicationEvent;

/**
 *  Published when an event to a Camera
 *
 * @author Maurizio Nagni
 */
public class CameraEvent extends ApplicationEvent {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5094032542760128396L;

	public CameraEvent(Object source) {
		super(source);
	}
}
