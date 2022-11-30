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

import java.io.Serializable;
import java.text.MessageFormat;


public class ImutableRectangularIntegerROI implements RectangularROI<Integer>, Serializable {

	/**
	 * Generated serial ID
	 */
	private static final long serialVersionUID = 4528518322504011015L;

	private final int xstart;
	private final int xsize;
	private final int ystart;
	private final int ysize;
	private final String name;

	public static ImutableRectangularIntegerROI valueOf(org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI scisoftRoi) {
		String name = scisoftRoi.getName();
		int xstart = (int) Math.round(scisoftRoi.getPointX());
		int ystart = (int) Math.round(scisoftRoi.getPointY());
		int xsize = (int) Math.round(scisoftRoi.getLength(0));
		int ysize = (int) Math.round(scisoftRoi.getLength(1));
		return new ImutableRectangularIntegerROI(xstart, ystart, xsize, ysize, name);
	}

	public ImutableRectangularIntegerROI(Integer xstart, Integer ystart, Integer xsize, Integer ysize, String name) {
		this.xstart = xstart;
		this.ystart = ystart;
		this.xsize = xsize;
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
		return MessageFormat.format("start=({0}, {1}) size={2}x{3} *{4}*", xstart, ystart, xsize, ysize, name);
	}

}
