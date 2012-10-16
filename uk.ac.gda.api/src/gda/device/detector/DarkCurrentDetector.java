/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.device.detector;

/**
 * A detector implementing this interface is capable of reading the
 * dark current in the atScanStart() method. This can be quered for 
 * real time maths on the count rate later on in the scan.
 */
public interface DarkCurrentDetector {

	/**
	 * 
	 * @return results from Dark Current reading, typically ordered I0, It, Iref
	 */
	public DarkCurrentResults getDarkCurrentResults();

}
