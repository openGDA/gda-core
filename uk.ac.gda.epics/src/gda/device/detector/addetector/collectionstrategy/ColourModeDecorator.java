/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableBiMap;

public class ColourModeDecorator extends AbstractADCollectionStrategyDecorator {

	private static final Logger logger = LoggerFactory.getLogger(ColourModeDecorator.class);

	private boolean restoreColourMode = false;

	private ColourMode colourMode;
	private int colourModeSaved;
	// We save the raw value so that this class can be used with cameras which also support non standard colour modes.

	public enum ColourMode {
		Mono,
		Bayer,
		RGB;

		private static final ImmutableBiMap<ColourMode, Integer> ColourModeToInteger =
			new ImmutableBiMap.Builder<ColourMode, Integer>()
				.put(Mono, 0)
				.put(Bayer, 1)
				.put(RGB, 2)
				.build();

		public Integer asInteger() {
			return ColourMode.ColourModeToInteger.get(this);
		}

		public static ColourMode fromInteger(Integer colourMode) {
			return ColourModeToInteger.inverse().get(colourMode);
		}
	}

	@Override
	protected void rawPrepareForCollection(double collectionTime, int numberImagesPerCollection, ScanInformation scanInfo) throws Exception {
		logger.trace("rawPrepareForCollection({}, {}, {}) called", collectionTime, numberImagesPerCollection, scanInfo);
		// getAdBase().setColorMode(ColourMode.ToInteger.get(colourMode));
		getAdBase().setColorMode(colourMode.asInteger());
		getDecoratee().prepareForCollection(collectionTime, numberImagesPerCollection, scanInfo);
	}

	// CollectionStrategyDecoratableInterface interface

	@Override
	public void saveState() throws Exception {
		logger.trace("saveState() called, restoreColourMode={}", restoreColourMode);
		getDecoratee().saveState();
		if (restoreColourMode) {
			colourModeSaved = getAdBase().getColorMode();
			logger.debug("Saved state to colourModeSaved={},{} (stop/restart={})",
				colourModeSaved, ColourMode.fromInteger(colourModeSaved), getAdBase().getAcquireState());
		}
	}

	@Override
	public void restoreState() throws Exception {
		final int acquireStatus = getAdBase().getAcquireState(); // TODO: Not all detectors need detector to be stopped to set time
		logger.trace("restoreState() called, restoreColourMode={}, acquireStatus={}", restoreColourMode, acquireStatus);
		if (restoreColourMode) {
			getAdBase().stopAcquiring();
			getAdBase().setColorMode(colourModeSaved);
			if (acquireStatus == 1) {
				getAdBase().startAcquiring();
			}
			logger.debug("Restored state to colourModeSaved={},{} (stop/restart={})",
				colourModeSaved, ColourMode.fromInteger(colourModeSaved), acquireStatus);
		}
		getDecoratee().restoreState();
	}

	// Class properties

	public boolean getRestoreColourMode() {
		return restoreColourMode;
	}

	public void setRestoreColourMode(boolean restoreColourMode) {
		this.restoreColourMode = restoreColourMode;
	}

	public ColourMode getColourMode() {
		return colourMode;
	}

	public void setColourMode(ColourMode colourMode) {
		this.colourMode = colourMode;
	}
}
