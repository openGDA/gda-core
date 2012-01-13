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

package uk.ac.gda.doe;

public enum DOEFieldType {
	
	SINGLE_VALUE,
	LIST,
	RANGE;

	/**
	 * Decodes a value to determine the DOEFieldType, if it cannot be decoded,
	 * SINGLE_VALUE is returned.
	 * 
	 * @param value
	 * @return DOEFieldType
	 */
	public static DOEFieldType getRangeType(String value) {
		
		if (value.indexOf(";")>-1 && value.split(";").length==3) {
			return RANGE;
		} else if (value.indexOf(",")>-1) {
			return LIST;
		}
		return SINGLE_VALUE;
	}
}
