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

package gda.data.scan.datawriter.scannablewriter;

/**
 * A simple example of a scannable with one input or extra name that needs 
 * to record in a specific place
 * 
 * Since we now have SingleScannableWriter which works for any number of input or extra names
 * this just refers to that and otherwise avoids the need to define single element lists in the 
 * Spring configuration 
 */
public class SimpleSingleScannableWriter extends SingleScannableWriter {

	public String getPath() {
		return getPaths()[0];
	}

	public void setPath(String path) {
		setPaths(new String[] {path});
	}

	public String getUnit() {
		return getUnits()[0];
	}

	public void setUnit(String unit) {
		setUnits(new String[] {unit});
	}
}