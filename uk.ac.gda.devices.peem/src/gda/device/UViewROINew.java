/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.device;

/**
 * An interface for a distributed PEEM Uview Detector class
 */
public interface UViewROINew extends Detector {

	/**
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @throws DeviceException 
	 */
	public void setBounds(int x, int y, int width, int height) throws DeviceException;

	/**
	 * @param x
	 * @param y
	 * @throws DeviceException 
	 */
	public void setLocation(int x, int y) throws DeviceException;

	/**
	 * @param width
	 * @param height
	 * @throws DeviceException 
	 */
	public void setSize(int width, int height) throws DeviceException;
}
