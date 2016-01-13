/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

import gda.scan.ScanInformation;

/**
 * Configure trigger mode.
 *
 * This strategy decorator can be used for detectors which implement non standard triggering mechanism.
 *
 * For the Standard Internal or External trigger modes, use {@link ExternalTriggerModeDecorator} or
 * {@link InternalTriggerModeDecorator}
 */
public class TriggerModeDecorator extends AbstractADCollectionStrategyDecorator {

	private boolean restoreTriggerMode = false;
	private int savedTriggerMode;

	private int triggerMode;
	private boolean triggerModeSet = false;

	// NXCollectionStrategyPlugin interface

	@Override
	protected void rawPrepareForCollection(double collectionTime, int numberImagesPerCollection, ScanInformation scanInfo) throws Exception {
		getAdBase().setTriggerMode(triggerMode);
		getDecoratee().prepareForCollection(collectionTime, numberImagesPerCollection, scanInfo);
	}

	// CollectionStrategyBeanInterface

	@Override
	public void saveState() throws Exception {
		getDecoratee().saveState();
		if (restoreTriggerMode) {
			savedTriggerMode = getAdBase().getTriggerMode();
		}
	}

	@Override
	public void restoreState() throws Exception {
		if (restoreTriggerMode) {
			getAdBase().setTriggerMode(savedTriggerMode);
		}
		getDecoratee().restoreState();
	}

	// InitializingBean interface

	@Override
	public void afterPropertiesSet() throws Exception {
		if (!triggerModeSet) throw new RuntimeException("triggerMode is not set");
		super.afterPropertiesSet();
	}

	// Class properties

	public int getTriggerMode() {
		return triggerMode;
	}

	public void setTriggerMode(int triggerMode) {
		errorIfPropertySetAfterBeanConfigured("triggerMode");
		this.triggerMode = triggerMode;
		this.triggerModeSet = true;
	}

	public boolean getRestoreTriggerMode() {
		return restoreTriggerMode;
	}

	public void setRestoreTriggerMode(boolean restoreTriggerMode) {
		this.restoreTriggerMode = restoreTriggerMode;
	}
}
