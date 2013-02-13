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

package gda.device.detector.areadetector.v17.impl;

import gda.device.detector.areadetector.v17.NDPluginBasePVs;
import gda.epics.LazyPVFactory;
import gda.epics.PV;
import gda.epics.PVWithSeparateReadback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class NDPluginBasePVsImpl implements NDPluginBasePVs, InitializingBean {
	
	static final Logger logger = LoggerFactory.getLogger(NDPluginBasePVsImpl.class);

	/**
	 * Map to PV names
	 */
	private enum PVNames {
		EnableCallbacks, EnableCallbacks_RBV, BlockingCallbacks, BlockingCallbacks_RBV, DroppedArrays, DroppedArrays_RBV, ArrayCounter, ArrayCounter_RBV, NDArrayPort, NDArrayPort_RBV 
	}

	private String basePVName;

	private PV<Boolean> enableCallbacksPVPair;

	private PV<Boolean> blockingCallbacksPVPair;

	private PV<Boolean> droppedArraysPVPair;

	private PV<Boolean> arrayCounterPVPair;
	
	private PV<String> ndArrayPortPVPair;

	public void setBasePVName(String basePVName) {
		this.basePVName = basePVName;
	}

	public String getBasePVName() {
		return basePVName;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (basePVName == null) {
			throw new IllegalArgumentException("'basePVName' needs to be declared");
		}
		createLazyPvs();
	}

	private void createLazyPvs() {

		enableCallbacksPVPair = new PVWithSeparateReadback<Boolean>(
				LazyPVFactory.newBooleanFromIntegerPV(fullname(PVNames.EnableCallbacks)),
				LazyPVFactory.newReadOnlyBooleanFromEnumPV(fullname(PVNames.EnableCallbacks_RBV)));
		
		blockingCallbacksPVPair = new PVWithSeparateReadback<Boolean>(
				LazyPVFactory.newBooleanFromIntegerPV(fullname(PVNames.BlockingCallbacks)),
				LazyPVFactory.newReadOnlyBooleanFromEnumPV(fullname(PVNames.BlockingCallbacks_RBV)));
		
		droppedArraysPVPair = new PVWithSeparateReadback<Boolean>(
				LazyPVFactory.newBooleanFromIntegerPV(fullname(PVNames.DroppedArrays)),
				LazyPVFactory.newReadOnlyBooleanFromEnumPV(fullname(PVNames.DroppedArrays_RBV)));
		
		arrayCounterPVPair = new PVWithSeparateReadback<Boolean>(
				LazyPVFactory.newBooleanFromIntegerPV(fullname(PVNames.ArrayCounter)),
				LazyPVFactory.newReadOnlyBooleanFromEnumPV(fullname(PVNames.ArrayCounter_RBV)));
		
		ndArrayPortPVPair = new PVWithSeparateReadback<String>(
				LazyPVFactory.newStringPV(fullname(PVNames.NDArrayPort)),
				LazyPVFactory.newReadOnlyStringPV(fullname(PVNames.NDArrayPort_RBV)));

	}

	private String fullname(Enum<?> pvName) {
		return basePVName + pvName.name();
	}

	@Override
	public PV<Boolean> getEnableCallbacksPVPair() {
		return enableCallbacksPVPair;
	}

	@Override
	public PV<Boolean> getBlockingCallbacksPVPair() {
		return blockingCallbacksPVPair;
	}

	@Override
	public PV<Boolean> getDroppedArraysPVPair() {
		return droppedArraysPVPair;
	}

	@Override
	public PV<Boolean> getArrayCounterPVPair() {
		return arrayCounterPVPair;
	}

	@Override
	public PV<String> getNDArrayPortPVPair() {
		return ndArrayPortPVPair;
	}

}
