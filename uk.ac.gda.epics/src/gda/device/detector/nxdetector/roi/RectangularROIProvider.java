/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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


public interface RectangularROIProvider<T extends Number> {
	
	/**
	 * Return a RectangularROI, or null if none is available/enabled.
	 * 
	 * @param index starting from 0
	 * @return an roi or null if none available/active
	 * @throws IllegalArgumentException if the index is out of bounds
	 * @throws Exception If the RCPPlotter.getBean throws an exception
	 * @throws IndexOutOfBoundsException 
	 */
	public RectangularROI<T> getROI(int index) throws IllegalArgumentException, IndexOutOfBoundsException, Exception;

}
