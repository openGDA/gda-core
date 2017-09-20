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
import gda.device.detector.pco.PCODriverController;
import gda.scan.ScanInformation;

/**
 * Configure PCO Arm mode.
 * PCO can only be armed or disarmed while the camera is stopped.
 * It requires a decoratee of {@link SoftwareStartStop} in its configuration to stop camera acquiring,
 * then arm the camera before next acquisition or scan.
 * The decoratee of {@link SoftwareStartStop} must be the inner-most decoratee,
 * which stops PCO camera before arming the camera.
 */
public class PCOArmDecorator extends AbstractADCollectionStrategyDecorator {
	private static final Logger logger = LoggerFactory.getLogger(PCOArmDecorator.class);
	private PCODriverController pcoController;
	private boolean restoreArm = false;

	private int armSaved;

	@Override
	protected void rawPrepareForCollection(double collectionTime, int numberImagesPerCollection, ScanInformation scanInfo) throws Exception {
		getPcoController().armCamera();
		getDecoratee().prepareForCollection(collectionTime, numberImagesPerCollection, scanInfo); // must be called before detector initialise
	}


	@Override
	public void afterPropertiesSet() throws Exception {
		if (getPcoController() == null) throw new IllegalStateException("'pcoController' is not set!");
		if (getDecoratee()==null) throw new IllegalStateException("'decoratee' is not set!");
		if (!(getDecoratee() instanceof SoftwareStartStop)) throw new IllegalStateException("arm decorator must have instance of SoftwareStartStop as decoratee!");
		super.afterPropertiesSet();
	}
	// CollectionStrategyDecoratableInterface interface

	@Override
	public void saveState() throws Exception {
		logger.trace("saveState() called, restoreArm={}", restoreArm);
		getDecoratee().saveState();
		if (isRestoreArm()) {
			armSaved=getPcoController().getArmMode();
			logger.debug("Saved State now armSaved={}", armSaved);
		}
	}

	@Override
	public void restoreState() throws Exception {
		logger.trace("restoreState() called, restoreArm={}, armSaved={}", restoreArm, armSaved);
		if (isRestoreArm()) {
			getPcoController().setArmMode(armSaved);
			logger.debug("Restored state to armSaved={}", armSaved);
		}
		getDecoratee().restoreState();
	}

	// Class properties

	public boolean isRestoreArm() {
		return restoreArm;
	}

	public void setRestoreArm(boolean restoreArm) {
		this.restoreArm = restoreArm;
	}

	public PCODriverController getPcoController() {
		return pcoController;
	}


	public void setPcoController(PCODriverController pcoparameters) {
		this.pcoController = pcoparameters;
	}
}
