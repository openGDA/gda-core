/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

import gda.factory.Findable;

/**
 * This class provides a mutable rectangular ROI which is also findable to allow the ROI on a area detector to be adjusted from the GDA command line/scripts. To
 * use on the command line it will need to be obtained using the Finder. To make it easier to integrate with ADRoiStatsPairFactory it also implements
 * {@link RectangularROIProvider} returning itself.
 * <p>
 * Example Spring XML:
 *
 * <pre>
 * {@code
 * <bean id="cam1_roi" class="gda.device.detector.nxdetector.roi.MutableRectangularIntegerROI">
 *	 <property name="xstart" value="0" />
 *	 <property name="ystart" value="0" />
 *	 <property name="xsize" value="500" />
 *	 <property name="ysize" value="500" />
 * </bean>
 * }
 * </pre>
 *
 * @author James Mudd
 */
public class MutableRectangularIntegerROI implements Findable, RectangularROI<Integer>, RectangularROIProvider<Integer> {

	private String name;
	// Just a note, I would have preferred to call these xStart, yStart, xSize and ySize however the method names in RectangularROI cause this to become messy.
	private Integer xstart;
	private Integer ystart;
	private Integer xsize;
	private Integer ysize;

	public MutableRectangularIntegerROI() {
		// No arg constructor
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public Integer getXstart() {
		return xstart;
	}

	public void setXstart(Integer xstart) {
		this.xstart = xstart;
	}

	@Override
	public Integer getYstart() {
		return ystart;
	}

	public void setYstart(Integer ystart) {
		this.ystart = ystart;
	}

	@Override
	public Integer getXsize() {
		return xsize;
	}

	public void setXsize(Integer xsize) {
		this.xsize = xsize;
	}

	@Override
	public Integer getYsize() {
		return ysize;
	}

	public void setYsize(Integer ysize) {
		this.ysize = ysize;
	}

	@Override
	public RectangularROI<Integer> getRoi() throws IllegalArgumentException, IndexOutOfBoundsException, Exception {
		return this;
	}

	/**
	 * Change this ROI to match a existing {@link RectangularROI}
	 *
	 * @param roi
	 *            Existing {@link RectangularROI}
	 */
	public void setROI(RectangularROI<Integer> roi) {
		this.xstart = roi.getXstart();
		this.xsize = roi.getXsize();
		this.ystart = roi.getYstart();
		this.ysize = roi.getYsize();
	}

	/**
	 * Convenience constructor to create a {@link MutableRectangularIntegerROI} from an existing {@link RectangularROI}.
	 *
	 * @param roi
	 *            Existing {@link RectangularROI}
	 */
	public MutableRectangularIntegerROI(RectangularROI<Integer> roi) {
		this.xstart = roi.getXstart();
		this.xsize = roi.getXsize();
		this.ystart = roi.getYstart();
		this.ysize = roi.getYsize();
	}

}
