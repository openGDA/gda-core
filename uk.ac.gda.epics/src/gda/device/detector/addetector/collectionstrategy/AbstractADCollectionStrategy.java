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
 * This class is intended as the base class for all Area Detector Collection strategies which are compatible with the new composition system. Currently it just
 * redirects to AbstractADTriggeringStrategy, but eventually it should replace it, with a minimal set of collection strategy functions, allowing
 * AbstractADCollectionStrategyDecorators to add most of the more complex code. Any functions that just delegate to AbstractADTriggeringStrategy should be added
 * to AbstractADCollectionStrategyBase, not to this class.
 */
public abstract class AbstractADCollectionStrategy extends AbstractADCollectionStrategyBase {

	// NXCollectionStrategyPlugin interface

	@Override
	public final void prepareForCollection(double collectionTime, int numberImagesPerCollection, ScanInformation scanInfo) throws Exception {
		beforePreparation();
		super.prepareForCollection(collectionTime, numberImagesPerCollection, scanInfo);
		rawPrepareForCollection(collectionTime, numberImagesPerCollection, scanInfo);
	}

	@Override
	public final void completeCollection() throws Exception {
		rawCompleteCollection();
		afterCompletion();
	}

	@Override
	public final void atCommandFailure() throws Exception {
		rawAtCommandFailure();
		afterCompletion();
	}

	@Override
	public final void stop() throws Exception {
		rawStop();
		afterCompletion();
	}

	// CollectionStrategyDecoratableInterface

	/*
	 * Default save/restore functions. Ideally, all collection strategies should save & restore any state they change, so it may eventually be possible to
	 * remove these default implementations.
	 */

	@Override
	public final void setSuppressSave() {
		suppressSave = true;
	}

	@Override
	public final void setSuppressRestore() {
		suppressRestore = true;
	}

	@Override
	public void saveState() throws Exception {
	}

	@Override
	public void restoreState() throws Exception {
	}

	/*
	 * Default versions of the "raw" functions called above
	 */
	@SuppressWarnings("unused")
	protected void rawPrepareForCollection(double collectionTime, int numberImagesPerCollection, ScanInformation scanInfo) throws Exception {
	}

	@SuppressWarnings("unused")
	protected void rawCompleteCollection() throws Exception {
	}

	@SuppressWarnings("unused")
	protected void rawAtCommandFailure() throws Exception {
	}

	@SuppressWarnings("unused")
	protected void rawStop() throws Exception {
	}

	// Class functions

	/**
	 * If prepareCollection() has been called by a decorator, the decorator will already have caused the collection strategy it to save its state, and will have
	 * set suppressSave. In this case, just reset the flag. Otherwise, save the state.
	 *
	 * @throws Exception
	 */
	private void beforePreparation() throws Exception {
		if (suppressSave) {
			suppressSave = false;
		} else {
			saveState();
		}
	}

	/**
	 * As with startup(), a decorator may be in control of restoring the state, so check the flag before restoring.
	 *
	 * @throws Exception
	 */
	private void afterCompletion() throws Exception {
		if (suppressRestore) {
			suppressRestore = false;
		} else {
			restoreState();
		}
	}
}
