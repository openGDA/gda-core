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

/**
 * Represents possible modes for a camera trigger.
 *
 * <p>
 * The enum value should be not considered directly connected with a camera trigger. The reason relies in the fact
 * that there is not a standard list of trigger consequently is not possible to define a definitive list.
 * </p>
 * <p>
 * However is possible to assume at least an <i>INTERNAL</i> and <i>EXTERNAL</i> trigger. The first in particular is useful to configure the camera
 * after acquisition engine, ie Malcolm, left the camera in a status where starting the acquisition is not enough to have a live stream.
 * See {@code @see uk.ac.diamond.daq.client.gui.camera.monitor.widget.CameraMonitorButtonHelper} and {@code CameraProperties.getTriggerMode()}
 * </p>
 *
 * @author Maurizio Nagni
 */
public enum TriggerMode {
	INTERNAL,
	EXTERNAL
}
