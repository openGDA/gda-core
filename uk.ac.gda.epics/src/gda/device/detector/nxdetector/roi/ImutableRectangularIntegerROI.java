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

import java.text.MessageFormat;


public class ImutableRectangularIntegerROI implements RectangularROI<Integer> {

	final private int xstart;
	final private int xsize;
	final private int ystart;
	final private int ysize;
	final private String name;

	public static ImutableRectangularIntegerROI valueOf(uk.ac.diamond.scisoft.analysis.roi.RectangularROI scisoftRoi) {
		String name = scisoftRoi.getName();
		int xstart = (int) scisoftRoi.getPointX();
		int xsize = (int) scisoftRoi.getLength(0);
		int ystart = (int) scisoftRoi.getPointY();
		int ysize = (int) scisoftRoi.getLength(1);
		return new ImutableRectangularIntegerROI(xstart, xsize, ystart, ysize, name);
	}
	
	public ImutableRectangularIntegerROI(Integer xstart, Integer xsize, Integer ystart, Integer ysize, String name) {
		this.xstart = xstart;
		this.xsize = xsize;
		this.ystart = ystart;
		this.ysize = ysize;
		this.name = name;
	}

	@Override
	public Integer getXstart() {
		return xstart;
	}

	@Override
	public Integer getXsize() {
		return xsize;
	}

	@Override
	public Integer getYstart() {
		return ystart;
	}

	@Override
	public Integer getYsize() {
		return ysize;
	}

	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return MessageFormat.format(
				"name:'{0}', xstart:{1}, ystart:{2}, xsize:{3}, ysize:{4}", name, xstart, ystart, xsize, ysize);
	}
}