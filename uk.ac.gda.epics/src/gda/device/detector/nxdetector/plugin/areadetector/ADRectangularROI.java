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

package gda.device.detector.nxdetector.plugin.areadetector;

public class ADRectangularROI {

	final private int xstart;
	final private int xsize;
	final private int ystart;
	final private int ysize;

	public ADRectangularROI(int xstart, int xsize, int ystart, int ysize) {
		this.xstart = xstart;
		this.xsize = xsize;
		this.ystart = ystart;
		this.ysize = ysize;
	}

	public int getXstart() {
		return xstart;
	}

	public int getXsize() {
		return xsize;
	}

	public int getYstart() {
		return ystart;
	}

	public int getYsize() {
		return ysize;
	}
}