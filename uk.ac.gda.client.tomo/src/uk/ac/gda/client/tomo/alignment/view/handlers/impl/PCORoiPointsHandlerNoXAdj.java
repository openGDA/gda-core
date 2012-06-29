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

package uk.ac.gda.client.tomo.alignment.view.handlers.impl;

import org.eclipse.draw2d.geometry.PointList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.tomo.alignment.view.handlers.IRoiHandler;

public class PCORoiPointsHandlerNoXAdj implements IRoiHandler {

	private static final Logger logger = LoggerFactory.getLogger(PCORoiPointsHandlerNoXAdj.class);
	
	@Override
	public void dispose() {
		//do nothing
	}

	@Override
	public PointList validatePoints(int direction, int x1, int y1, int x2, int y2) {
		logger.debug("Readjusting the x point back to initial position");
		return new PointList(new int[] { 0, y1, 4008, y2 });
	}

}
