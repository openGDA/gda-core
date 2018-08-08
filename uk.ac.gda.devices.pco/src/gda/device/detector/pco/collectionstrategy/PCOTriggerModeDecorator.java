/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package gda.device.detector.pco.collectionstrategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.detector.addetector.collectionstrategy.AbstractADCollectionStrategyDecorator;
import gda.device.detector.addetector.collectionstrategy.SoftwareStartStop;
import gda.device.detector.pco.PCOTriggerMode;
import gda.scan.ScanInformation;

/**
 * Configure PCO trigger mode.
 * PCO Trigger mode can only be changed when acquisition is stopped.
 * Thus it requires a decoratee of {@link SoftwareStartStop} in its configuration.
 * The decoratee of {@link SoftwareStartStop} must be the inner-most decoratee,
 * which stops PCO camera before applying trigger mode set here.
 */
public class PCOTriggerModeDecorator extends AbstractADCollectionStrategyDecorator {

	private static final Logger logger = LoggerFactory.getLogger(PCOTriggerModeDecorator.class);
	private PCOTriggerMode triggerMode;
	private boolean restoreTriggerMode;
	private short triggerModeSaved;
	/**
	 * Call to decoratee must be before change trigger mode for PCO detector.
	 * It relies on a decoratee of {@link SoftwareStartStop} to stop acquiring first,
	 * otherwise the trigger mode change will not take effect.
	 */
	@Override
	protected void rawPrepareForCollection(double collectionTime, int numberImagesPerCollection, ScanInformation scanInfo) throws Exception {
		getAdBase().setTriggerMode(triggerMode.ordinal());
		getDecoratee().prepareForCollection(collectionTime, numberImagesPerCollection, scanInfo);
	}

	// CollectionStrategyBeanInterface

	@Override
	public void saveState() throws Exception {
		logger.trace("saveState() called, triggerModeSaved={}", isRestoreTriggerMode());
		getDecoratee().saveState();
		if (isRestoreTriggerMode()) {
			triggerModeSaved = getAdBase().getTriggerMode();
			logger.debug("Saved State now triggerModeSaved={}", triggerModeSaved);
		}
	}

	@Override
	public void restoreState() throws Exception {
		logger.trace("restoreState() called, restoreTriggerMode={}, triggerModeSaved={}", isRestoreTriggerMode(), triggerModeSaved);
		if (isRestoreTriggerMode()) {
			getAdBase().setTriggerMode(triggerModeSaved);
			logger.debug("Restored state to triggerModeSaved={} (stop/restart=NA)", triggerModeSaved);
		}
		getDecoratee().restoreState();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (triggerMode==null) throw new IllegalStateException("'triggerMode' is not set!");
		if (getDecoratee()==null) throw new IllegalStateException("'decoratee' is not set!");
		super.afterPropertiesSet();
	}

	public void setTriggerMode(PCOTriggerMode mode) {
		this.triggerMode=mode;
	}

	public PCOTriggerMode getTriggerMode() {
		return triggerMode;
	}

	public boolean isRestoreTriggerMode() {
		return restoreTriggerMode;
	}

	public void setRestoreTriggerMode(boolean restoreTriggerMode) {
		this.restoreTriggerMode = restoreTriggerMode;
	}

}
