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

package gda.scan;

import java.util.Map;

/**
 * An interface which can be provided to ScanDataPoints and changes
 * the way they are printed to console and file.
 */
public interface ScanDataPointFormatter {
	
	/**
	 * 
	 * @return The data string
	 */
	public String getData(IScanDataPoint currentPoint, Map<String,String> data);
	
	/**
	 * 
	 * @return The header string
	 */
	public String getHeader(IScanDataPoint currentPoint, Map<String,String> data);
	
	
	/**
	 * 
	 * @param dataPoint
	 * @return true if formatter should be used
	 */
	public boolean isValid(IScanDataPoint dataPoint);
}
