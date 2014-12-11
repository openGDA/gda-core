/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package gda.device.detector.addetector.triggering;

import gda.device.detector.areadetector.v17.ADBase;
import gda.epics.LazyPVFactory;
import gda.epics.PV;
import gda.scan.ScanInformation;

public class PSLSingleExposure extends SimpleAcquire {

	private double collectionTime;
	private PV<Integer> resetConectionPV;
	private PV<Double> acquirePV;

	public enum PSLImageMode {
		MULTIPLE, CONTINUOUS
	}

	/**
	 * resetConnectionPVName and acquirePVName maybe null or empty strings.
	 * <p>
	 * If they are not then for each collection the connection to the hardware will be re-established and the collection
	 * time PV re-entered.
	 * <p>
	 * This is necessary based on experience with the hardware.
	 */
	public PSLSingleExposure(ADBase adBase, double readoutTime, String resetConnectionPVName, String acquirePVName) {
		super(adBase, readoutTime);
		if (readoutTime >= 0) {
			throw new IllegalArgumentException(
					"This detector does not support acquistion period. Please set the readout time to be negative to indicate that it will not be used.");
		}
		if (resetConnectionPVName != null && !resetConnectionPVName.isEmpty()) {
			resetConectionPV = LazyPVFactory.newIntegerPV(resetConnectionPVName);
		}
		if (acquirePVName != null && !acquirePVName.isEmpty()) {
			acquirePV = LazyPVFactory.newDoublePV(acquirePVName);
		}
	}

	@Override
	public void configureAcquireAndPeriodTimes(double collectionTime) throws Exception {
		this.collectionTime = collectionTime;
		// there is no acquire period pv visible for this detector
		getAdBase().setAcquireTime(collectionTime);
	}

	@Override
	public void prepareForCollection(double collectionTime, int numImages, ScanInformation scanInfo) throws Exception {
		super.prepareForCollection(collectionTime, 1, scanInfo);
		// This detector has no trigger mode
		getAdBase().setImageMode(PSLImageMode.MULTIPLE.ordinal());
	}

	@Override
	public void collectData() throws Exception {
		if (resetConectionPV != null) {
			resetConectionPV.putWait(1);
		}

		if (acquirePV != null) {
			acquirePV.putWait(collectionTime);
		}

		if (getAdBase().getAcquireState() == 0) {
			getAdBase().startAcquiring();
		}
	}
}