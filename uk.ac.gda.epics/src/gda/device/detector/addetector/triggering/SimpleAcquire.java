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

package gda.device.detector.addetector.triggering;

import gda.device.DeviceException;
import gda.device.detector.areadetector.v17.ADBase;


public class SimpleAcquire extends AbstractADTriggeringStrategy {

	
	public SimpleAcquire(ADBase adBase, double readoutTime) {
		super(adBase);
		setReadoutTime(readoutTime);
	}

	@Override
	public void prepareForCollection(double collectionTime, int numImages) throws Exception {
		if (numImages != 1) {
			throw new IllegalArgumentException("This single exposure triggering strategy expects to expose only 1 image");
		}
		configureAcquireAndPeriodTimes(collectionTime);
		getAdBase().setNumImages(numImages);
	}

	@Override
	public double getAcquireTime() throws Exception {
		return getAdBase().getAcquireTime_RBV();
	}

	@Override
	public double getAcquirePeriod() throws Exception {
		return getAdBase().getAcquirePeriod_RBV();
	}
	
	@Override
	public void collectData() throws Exception {
		getAdBase().startAcquiring();
	}

	@Override
	public int getStatus() throws DeviceException {
		return getAdBase().getStatus();
	}
	
	@Override
	public void waitWhileBusy() throws InterruptedException, DeviceException {
		getAdBase().waitWhileStatusBusy();
	}
	
	@Override
	public void stop() throws Exception {
		endCollection();
	}
	
	@Override
	public void atCommandFailure() throws Exception {
		endCollection();
	}
	
	@Override
	public void endCollection() throws Exception {
		getAdBase().stopAcquiring();
	}

	@Override
	public int getNumberImagesPerCollection(double collectionTime) {
		return 1;
	}

}
