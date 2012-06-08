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

package gda.scan;

import gda.scan.IScanStepId;

/**
 * Test implementation of IScanStepId.
 * If needs to be in uk.ac.gda.core for access to ScanDataPoint deserialization from ScanDataPointVar
 */
public class LocalStepId implements IScanStepId{
	String label;
	public LocalStepId(String label){
		this.label = label;
	}
	@Override
	public String asLabel() {
		return label;
	}
	
}