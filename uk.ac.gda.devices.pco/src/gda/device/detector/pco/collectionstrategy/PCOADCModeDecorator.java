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
import gda.device.detector.pco.PCOADCMode;
import gda.device.detector.pco.PCODriverController;
import gda.scan.ScanInformation;

/**
 * Configure PCO ADC mode.
 * PCO ADC mode can only be changed when camera is stopped.
 * Thus it requires a decoratee of {@link SoftwareStartStop} in its configuration.
 * The decoratee of {@link SoftwareStartStop} must be the inner-most decoratee,
 * which stops PCO camera before applying ADC mode setting here.
 */
public class PCOADCModeDecorator extends AbstractADCollectionStrategyDecorator {
	private static final Logger logger = LoggerFactory.getLogger(PCOADCModeDecorator.class);
	private PCODriverController pcoController;
	private PCOADCMode adcMode;
	private boolean restoreADCMode = false;
	private int adcModeSaved;

	@Override
	protected void rawPrepareForCollection(double collectionTime, int numberImagesPerCollection, ScanInformation scanInfo) throws Exception {
		getPcoController().setADCMode(getAdcMode());
		getDecoratee().prepareForCollection(collectionTime, numberImagesPerCollection, scanInfo);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (getPcoController() == null) throw new IllegalStateException("'pcoController' is not set!");
		if (getDecoratee()==null) throw new IllegalStateException("'decoratee' is not set!");
		if (adcMode==null) throw new IllegalStateException("'adcMode' is not set!");
		super.afterPropertiesSet();
	}

	// CollectionStrategyDecoratableInterface interface

	@Override
	public void saveState() throws Exception {
		logger.trace("saveState() called, restoreADCMode={}", restoreADCMode);
		getDecoratee().saveState();
		if (isRestoreADCMode()) {
			adcModeSaved=getPcoController().getADCMode();
			logger.debug("Saved State now adcModeSaved={}", adcModeSaved);
		}
	}

	@Override
	public void restoreState() throws Exception {
		logger.trace("restoreState() called, restoreADCMode={}, adcModeSaved={}", restoreADCMode, adcModeSaved);
		if (isRestoreADCMode()) {
			getPcoController().setADCMode(adcModeSaved);
			logger.debug("Restored state to adcModeSaved={}", adcModeSaved);
		}
		getDecoratee().restoreState();
	}

	// Class properties

	public boolean isRestoreADCMode() {
		return restoreADCMode;
	}

	public void setRestoreADCMode(boolean restoreADCMode) {
		this.restoreADCMode = restoreADCMode;
	}

	public PCOADCMode getAdcMode() {
		return adcMode;
	}

	public void setAdcMode(PCOADCMode adcMode) {
		this.adcMode = adcMode;
	}

	public PCODriverController getPcoController() {
		return pcoController;
	}

	public void setPcoController(PCODriverController pcocontroller) {
		this.pcoController = pcocontroller;
	}

}
