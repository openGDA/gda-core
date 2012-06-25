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

package uk.ac.gda.client.tomo.alignment.view.handlers;

import org.eclipse.draw2d.geometry.PointList;

/**
 *
 */
public interface IRoiHandler extends ITomoHandler {

	/**
	 * Validates the given points against the hardware.
	 * 
	 * @param direction
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return list of valid points which are set back on the GUI
	 */
	PointList validatePoints(int direction, int x1, int y1, int x2, int y2);

}
