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

package gda.scan;

import gda.util.TypeConverters;

import java.util.Vector;


/**
 *
 */
public class ScanStepId implements IScanStepId {
	@Override
	public String asLabel() {
		return toString();
	}

	String scannableId;
	Object value;

	/**
	 * @param scannableId
	 * @param value
	 */
	public ScanStepId(String scannableId, Object value)
	{
		this.scannableId = scannableId;
		this.value = value;
	}

	@Override
	public String toString() {
		String stepIdAsString = scannableId + "=";
		if( value != null){
			Vector<String>vals = new Vector<String>();
			TypeConverters.makeStringsFromObject(vals, value);
			for(String d : vals){
				stepIdAsString += d + ",";
			}
		} else {
			stepIdAsString += "unknown";
		}
		return stepIdAsString;
	}

}
