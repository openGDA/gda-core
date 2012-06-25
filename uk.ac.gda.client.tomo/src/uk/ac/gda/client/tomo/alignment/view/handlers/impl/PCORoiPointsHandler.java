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
import org.eclipse.swt.SWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.tomo.alignment.view.handlers.IRoiHandler;

/**
 *
 */
public class PCORoiPointsHandler implements IRoiHandler {
	private static final Logger logger = LoggerFactory.getLogger(PCORoiPointsHandler.class);

	@Override
	public PointList validatePoints(int direction, int x1, int y1, int x2, int y2) {
		int x1Copy = x1;
		int x2Copy = x2;
		if (SWT.LEFT == direction) {
			// if x1 = 34
			// applying 4n+1 = 34 => n = 33/4
			// so make x1 = 33
			// then n = 32/4 = 8
			// then x2 = 4008 - 4(8)

			if (x1 == 0) {
				x1 = 0;
				x2 = 4008;

			} else {

				while (x1 > 0 && (x1 - 1) % 4 != 0) {
					x1 = x1 - 1;
				}

				int n = (x1 - 1) / 4;
				logger.debug("n is {}", n);
				// 4008 should be camerahandler.getFullImagewidth()
				x2 = 4008 - (4 * n);
			}
		} else if (SWT.RIGHT == direction) {
			if (x2 == 4008) {
				x1 = 0;
				x2 = 4008;
			} else {
				while (x2 < 4008 & (x2 + 1) % 4 != 0) {
					x2 = x2 + 1;
				}
				int n = (x2 + 1) / 4;
				logger.debug("n is {}", n);
				x1 = 4008 - (4 * n);
			}
		}

		if (x1 > x2) {
			// If the points
			x1 = x1Copy;
			x2 = x2Copy;
			if (SWT.RIGHT == direction) {
				while (x1 > 0 && (x1 - 1) % 4 != 0) {
					x1 = x1 - 1;
				}

				int n = (x1 - 1) / 4;
				logger.debug("n is {}", n);
				// 4008 should be camerahandler.getFullImagewidth()
				x2 = 4008 - (4 * n);
			} else {
				while (x2 < 4008 & (x2 + 1) % 4 != 0) {
					x2 = x2 + 1;
				}
				int n = (x2 + 1) / 4;
				logger.debug("n is {}", n);
				x1 = 4008 - (4 * n);
			}
		}
		logger.debug(String.format("x1: %d   --y1: %d   -- x2:%d   --y2:%d ", x1, y1, x2, y2));

		return new PointList(new int[] { x1, y1, x2, y2 });

	}
	
	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}
}
