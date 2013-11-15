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
import gda.epics.CachedLazyPVFactory;
import gda.epics.LazyPVFactory;
import gda.epics.PV;

public class ADDriverPcoImpl implements ADDriverPco, InitializingBean{


	private String basePvName;
	CachedLazyPVFactory dev;
	
	private PV<Boolean> armModePV;

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
		dev = new CachedLazyPVFactory(basePvName+":");
		
	}

	@Override
	public PV<Boolean> getArmModePV() {
		return armModePV;
	}

	@Override
	public PV<Double> getCameraUsagePV() {
		return dev.getPVDouble("CAM_RAM_USE_RBV");
	}

	@Override
	public PV<Integer> getAdcModePV() {
		return dev.getPVInteger("ADC_MODE");
	}

	@Override
	public PV<Integer> getTimeStampModePV() {
		return dev.getPVInteger("TIMESTAMP_MODE");
	}

	@Override
	public PV<Integer> getBinXPV() {
		return dev.getPVInteger("BinX");
	}

	@Override
	public PV<Integer> getBinYPV() {
		return dev.getPVInteger("BinY");
	}

	@Override
	public PV<Integer> getMinXPV() {
		return dev.getPVInteger("MinX");
	}

	@Override
	public PV<Integer> getSizeXPV() {
		return dev.getPVInteger("SizeX");
	}

	@Override
	public PV<Integer> getMinYPV() {
		return dev.getPVInteger("MinY");
	}

	@Override
	public PV<Integer> getSizeYPV() {
		return dev.getPVInteger("SizeY");
	}
	
	
}
