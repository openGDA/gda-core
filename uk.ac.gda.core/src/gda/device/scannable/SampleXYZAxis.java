/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.device.scannable;


/**
 * Base class for Scannables representing an axis of an MX XYZ sample support stage
 * <p>
 * To use: create three individual axis Scannables and then wrap them in a ScannableGroup object named using the static
 * string in this class.
 */
public abstract class SampleXYZAxis extends ScannableMotionUnitsBase {

	/**
	 * 
	 */
	public static final int X = 0;
	/**
	 * 
	 */
	public static final int Y = 1;
	/**
	 * 
	 */
	public static final int Z = 2;
	
	
	/**
	 * The name of a ScannableGroup holding the three axes.
	 */
	public static final String XYZ_SCANNABLE = "samplexyz";

	/**
	 * The name of an alternative ScannableGroup holding three axes which achieve the same movement using different 
	 * motors (e.g. goniometer support instead of sample stage).
	 */
	public static final String XYZ_SCANNABLE_ALT = "samplexyz_alt";

	private int axis;

	/**
	 * @return Returns the axis.
	 */
	public int getAxis() {
		return axis;
	}

	/**
	 * @param axis
	 *            The axis to set.
	 */
	public void setAxis(int axis) {
		this.axis = axis;
	}

}
