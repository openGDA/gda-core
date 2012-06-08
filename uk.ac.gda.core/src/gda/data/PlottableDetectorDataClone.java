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

/**
 * Simplifies a possibly richer detector return object into something that only holds
 * doubles for plotting and the String to print on the Terminal. This object will be 
 * sent to the GUI client.
 */
public class PlottableDetectorDataClone implements PlottableDetectorData {

	private Double[] vals;
	private String toStringString;

	@SuppressWarnings("unused") // that's th whole idea
	private PlottableDetectorDataClone() {}
	
	/**
	 * build one from an existing
	 * 
	 * @param pdd
	 */
	public PlottableDetectorDataClone(PlottableDetectorData pdd) {
		this.vals = pdd.getDoubleVals();
		this.toStringString = pdd.toString();
	}

	@Override
	public Double[] getDoubleVals() {
		return vals;
	}

	@Override
	public String toString() {
		return toStringString;
	}
}
