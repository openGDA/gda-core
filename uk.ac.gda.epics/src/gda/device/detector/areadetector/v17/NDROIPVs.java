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

package gda.device.detector.areadetector.v17;

import gda.epics.PV;
import gda.epics.ReadOnlyPV;


public interface NDROIPVs {
	
	public enum NDDataType {
		INT8,
		UINT8,
		INT16,
		UINT16,
		INT32,
		UINT32,
		FLOAT64,
		AUTOMATIC
	}
	
	public interface  ROIDimensionPVs { 
		
		PV<Boolean> getEnablePV();
		
		ReadOnlyPV<Integer> getMaxSizePV();
		
		PV<Integer>  getBinPVPair();

		PV<Integer>  getStartPVPair();

		PV<Integer>  getSizePVPair();

		PV<Boolean>  getReversePV();
		
		ReadOnlyPV<Integer> getArraySizePV();
	} 
	
	public NDPluginBasePVs getPluginBasePVs();
	
	PV<String> getNamePV();
	
	PV<NDDataType> getDataTypeOutPV();

	PV<Boolean> getEnableScalePVPair();
	
	PV<Integer> getScalePV();
	
	ROIDimensionPVs getXDimension();
	
	ROIDimensionPVs getYDimension();
	
	ROIDimensionPVs getZDimension();
	
}
