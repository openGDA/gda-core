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

package gda.device.detector.addetector.collectionstrategy;

import gda.device.detector.addetector.triggering.AbstractADTriggeringStrategy;
import gda.device.detector.addetector.triggering.UnsynchronisedExternalShutterNXCollectionStrategy;
import gda.device.detector.nxdetector.AbstractCollectionStrategyDecorator;
import gda.device.detector.nxdetector.CollectionStrategyBeanInterface;
import gda.scan.ScanInformation;

public final class UnsynchronisedExternalShutterDecorator extends AbstractCollectionStrategyDecorator
		implements UnsynchronisedExternalShutterNXCollectionStrategy {

	private double collectionExtensionTimeS = 5.0;

	public UnsynchronisedExternalShutterDecorator() {
	}

	/**
	 * Override decorated class in order to silently extend detector acquisition time.
	 * 
	 * On beamlines where an external shutter is used to control the length of an exposure and the trigger/gate for the shutter
	 * is not also sent to the detector, the acquisition must enclose the shutter acquisition time. Thus the actual acquisition
	 * needs to be the requested acquisition time, plus the time of the longest possible run-up.
	 * 
	 * Since the actual time of the run-up is variable, based on the target speed and timeToVelocity of the motor, and some
	 * detectors need a constant acquire time (PE needs a new dark to be taken every time the acquire time changes for instance)
	 * for the moment we have to use a single fixed time for the triggering strategy.
	 * 
	 * Note, the collectionExtensionTimeS value should be as short as possible to prevent unnecessary delays.
	 */

	@Override
	public void prepareForCollection(double collectionTime, int numImages, ScanInformation scanInfo) throws Exception {
		getDecoratee().prepareForCollection(collectionTime + collectionExtensionTimeS, numImages, scanInfo);
	}

	@Override
	public double getAcquireTime() throws Exception {
		return getDecoratee().getAcquireTime() - collectionExtensionTimeS;
	}

	@Override
	public double getAcquirePeriod() throws Exception {
		// TODO: Note there ware some differences between SingleExposureUnsynchronisedExternalShutter and
		//       MultipleExposureSoftwareTriggerAutoModeUnsynchronisedExternalShutter which suggest that this may
		//       require more testing an experimentation to get right.
		double acquirePeriod_RBV = super.getAcquirePeriod();
		CollectionStrategyBeanInterface decoratee = getDecoratee();
		if (decoratee instanceof AbstractADTriggeringStrategy) {
			//AbstractADTriggeringStrategy a = (AbstractADTriggeringStrategy) decoratee;
			if (((AbstractADTriggeringStrategy)decoratee).getReadoutTime() > 0) {
				acquirePeriod_RBV -= collectionExtensionTimeS;
			}
		}
		return acquirePeriod_RBV; 
	}

	@Override
	public int getNumberImagesPerCollection(double collectionTime) throws Exception {
		return getDecoratee().getNumberImagesPerCollection(collectionTime + collectionExtensionTimeS);
	}

	/* Getters and setters for private fields. */

	@Override
	public double getCollectionExtensionTimeS() {
		return collectionExtensionTimeS;
	}

	@Override
	public void setCollectionExtensionTimeS(double collectionExtensionTimeS) {
		this.collectionExtensionTimeS = collectionExtensionTimeS;
	}
}
