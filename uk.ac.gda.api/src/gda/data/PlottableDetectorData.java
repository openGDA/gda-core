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

package gda.data;

import java.io.Serializable;

/**
 * Preferred interface that data returned in readOut of a Detector should implement to allow the 
 * data to be plotted easily
 */
public interface PlottableDetectorData extends Serializable {

	/**
	 * @return Array of doubles - the length must match the length of the detector's extraNames. 
	 * null items are allowed and will not be plotted.
	 */
	public Double[] getDoubleVals();

}
