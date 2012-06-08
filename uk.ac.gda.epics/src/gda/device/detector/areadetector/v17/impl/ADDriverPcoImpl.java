/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.device.detector.areadetector.v17.impl;

import org.springframework.beans.factory.InitializingBean;

import gda.device.detector.areadetector.v17.ADDriverPco;
import gda.epics.LazyPVFactory;
import gda.epics.PV;

public class ADDriverPcoImpl implements ADDriverPco, InitializingBean{


	private String basePvName;
	
	private PV<Boolean> armModePV;
	private PV<Double> cameraUsagePV;

	private PV<Integer> adcModePV;

	@Deprecated // replace with a proper ADDriverPco - when it has been written!
	public void setBasePvName(String basePvName) {
		this.basePvName = basePvName;
	}

	public String getArmModePvName() {
		return basePvName;
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		if (basePvName == null) {
			throw new IllegalArgumentException("armModePvName must be set");
		}
		armModePV = LazyPVFactory.newBooleanFromEnumPV(basePvName + ":ARM_MODE"); 
		cameraUsagePV = LazyPVFactory.newDoublePV(basePvName + ":CAM_RAM_USE_RBV"); 
		adcModePV = LazyPVFactory.newIntegerPV(basePvName + ":ADC_MODE");
	}

	@Override
	public PV<Boolean> getArmModePV() {
		return armModePV;
	}

	@Override
	public PV<Double> getCameraUsagePV() {
		return cameraUsagePV;
	}

	@Override
	public PV<Integer> getAdcModePV() {
		return adcModePV;
	}
	
	
}
