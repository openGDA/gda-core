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

package gda.device.detector.nxdetector.roi;


public class ImutableRectangularROI implements RectangularROI {

	final private int xstart;
	final private int xsize;
	final private int ystart;
	final private int ysize;
	final private String name;

	public ImutableRectangularROI(int xstart, int xsize, int ystart, int ysize, String name) {
		this.xstart = xstart;
		this.xsize = xsize;
		this.ystart = ystart;
		this.ysize = ysize;
		this.name = name;
	}

	@Override
	public int getXstart() {
		return xstart;
	}

	@Override
	public int getXsize() {
		return xsize;
	}

	@Override
	public int getYstart() {
		return ystart;
	}

	@Override
	public int getYsize() {
		return ysize;
	}

	@Override
	public String getName() {
		return name;
	}
}