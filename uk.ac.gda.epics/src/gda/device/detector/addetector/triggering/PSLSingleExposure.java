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
import gda.scan.ScanInformation;

public class PSLSingleExposure extends SimpleAcquire {

	public enum PSLImageMode {
		MULTIPLE, CONTINUOUS
	}
	
	public PSLSingleExposure(ADBase adBase, double readoutTime) {
		super(adBase, readoutTime);
		if (readoutTime >= 0 ) {
			throw new IllegalArgumentException("This detector does not support acquistion period. Please set the readout time to be negative to indicate that it will not be used.");
		}
	}

	@Override
	public void configureAcquireAndPeriodTimes(double collectionTime) throws Exception {
		// there is no acquire period pv visible for this detecot
		getAdBase().setAcquireTime(collectionTime);
	}
	
	@Override
	public void prepareForCollection(double collectionTime, int numImages, ScanInformation scanInfo) throws Exception {
		super.prepareForCollection(collectionTime, 1, scanInfo);
		// This detector has no trigger mode
		getAdBase().setImageMode(PSLImageMode.MULTIPLE.ordinal());
	}

}