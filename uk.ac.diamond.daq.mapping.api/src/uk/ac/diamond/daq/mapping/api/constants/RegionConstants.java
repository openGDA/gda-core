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

package uk.ac.diamond.daq.mapping.api.constants;

import java.beans.PropertyChangeEvent;

public final class RegionConstants {

	public static final String X_POSITION = "xPosition";
	public static final String Y_POSITION = "yPosition";

	public static final String X_START = "xStart";
	public static final String Y_START = "yStart";
	public static final String X_STOP = "xStop";
	public static final String Y_STOP = "yStop";

	public static final String X_CENTRE = "xCentre";
	public static final String Y_CENTRE = "yCentre";
	public static final String X_RANGE = "xRange";
	public static final String Y_RANGE = "yRange";

	public static final int X = 0;
	public static final int Y = 1;

	public static final String RADIUS = "radius";

	public static final String ORIENTATION = "orientation";
	public static final String CONSTANT = "constant";
	public static final String START = "start";
	public static final String STOP = "stop";

	/**
	 * An update to the region could modify multiple parameters,
	 * each producing their own {@link PropertyChangeEvent}s.
	 * At the end of the update, an event with this property name will be fired;
	 * at this point the entire region can be consistently interrogated.
	 */
	public static final String UPDATE_COMPLETE = "updateComplete";

	private RegionConstants() {
		// prevent instantiation
	}
}
