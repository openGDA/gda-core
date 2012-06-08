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

package uk.ac.gda.analysis.hdf5;

public class HDF5HelperLocation {
	public final String name;
	public final String attributeName;
	public final String attributeValue;
	public HDF5HelperLocation(String name, String attributeName, String attributeValue) {
		super();
		this.name = name;
		this.attributeName = attributeName;
		this.attributeValue = attributeValue;
	}
	@Override
	public String toString() {
		return "HDF5HelperLocation [name=" + name + ", attributeName=" + attributeName + ", attributeValue="
				+ attributeValue + "]";
	}
	
}
