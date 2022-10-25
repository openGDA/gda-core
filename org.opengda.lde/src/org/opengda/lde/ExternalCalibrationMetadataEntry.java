/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package org.opengda.lde;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.metadata.IMetadataEntry;
import gda.factory.FactoryException;
import gda.jython.ITerminalPrinter;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;

/**
 * An IMetadataEntry that can act as a handler for {@link ReductionResponse}
 * messages.
 * <br>
 * Users can override the most recent calibration via the metadata instance
 * as they would with any other metadata entry.
 */
public class ExternalCalibrationMetadataEntry implements Consumer<ReductionResponse>, IMetadataEntry {
	private static final Logger logger = LoggerFactory.getLogger(ExternalCalibrationMetadataEntry.class);

	/** Printer to display messages to user */
	private transient ITerminalPrinter terminal;
	private transient ObservableComponent obs = new ObservableComponent();

	/** Name used to access this metadata entry */
	private String name;
	/**
	 * The currently configured calibration file. Can be set directly or
	 * via message from external process.
	 */
	private String latestCalibrationFile;

	@Override
	public void accept(ReductionResponse t) {
		switch (t.getStatus()) {
		case ERROR:
			logger.error("Error from external calibration: {}", t.getMessage());
			terminal.print("Error from calibration: " + t.getMessage());
			latestCalibrationFile = null;
			break;
		case WARN:
			logger.warn("Warning from external processing: {}", t.getMessage());
			terminal.print("Warning from calibration: " + t.getMessage());
			// $FALL-THROUGH$ // Still want to set the file name if there is one
		case OK:
			logger.debug("External calibration complete");
			terminal.print("Calibration complete. New calibration file: " + t.getCalibrationFilepath());
			latestCalibrationFile = t.getCalibrationFilepath();
		}
	}

	@Override
	public void addIObserver(IObserver anIObserver) {
		obs.addIObserver(anIObserver);
	}

	@Override
	public void deleteIObserver(IObserver anIObserver) {
		obs.deleteIObserver(anIObserver);
	}

	@Override
	public void deleteIObservers() {
		obs.deleteIObservers();
	}

	@Override
	public void configure() throws FactoryException {
		// only required via IMetadataEntry
	}

	@Override
	public boolean isConfigured() {
		// only required via IMetadataEntry
		return true;
	}

	@Override
	public void reconfigure() throws FactoryException {
		// only required via IMetadataEntry
	}

	@Override
	public boolean isConfigureAtStartup() {
		// only required via IMetadataEntry
		return false;
	}

	@Override
	public String getMetadataValue() {
		return latestCalibrationFile;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setValue(String metadataValue) throws Exception {
		latestCalibrationFile = metadataValue;
	}

	@Override
	public String getDefEntryName() {
		// only required via IMetadataEntry
		return null;
	}

	@Override
	public void setDefEntryName(String defEntryName) {
		// only required via IMetadataEntry
	}

	@Override
	public boolean canStoreValue() {
		// only required via IMetadataEntry
		return true;
	}

	@Override
	public String toString() {
		return String.format("Metadata: %s = %s", name, latestCalibrationFile);
	}

	public ITerminalPrinter getTerminal() {
		return terminal;
	}

	public void setTerminal(ITerminalPrinter terminal) {
		this.terminal = terminal;
	}
}
