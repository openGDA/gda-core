/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package org.opengda.lde.ui.providers;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.epics.LazyPVFactory;
import gda.epics.PV;
import gda.factory.ConfigurableBase;
import gda.factory.FactoryException;
import gov.aps.jca.event.MonitorEvent;

/**
 * Title provider for live plot view
 * <p>
 * If a file is being written when a title is requested, use that else return a configurable
 * default title.
 */
public class LivePlotTitleProvider extends ConfigurableBase implements Supplier<String> {
	private static final Logger logger = LoggerFactory.getLogger(LivePlotTitleProvider.class);

	private String capturePV;
	private String fullFilenamePV;
	private String arrayIdPV;

	/* File name fudging pvs */
	private String filepathPV;
	private String filenamePV;
	private String nextNumberPV;
	private String fileformatPV;

	private PV<String> filepath;
	private PV<String> filename;
	private PV<Integer> nextNumber;
	private PV<String> fileformat;


	/** The PV to provide the current filename */
	private PV<String> fullFilename;
	/** The PV to provide the current file writing state */
	private PV<Boolean> capture;

	/** The title to use if file is not being a written or PVs are not readable */
	private String defaultTitle;

	/** The current file writing state */
	private volatile Boolean capturing;

	/** The current filename. Null if most recent collection wasn't written to file */
	private String currentFullFilename;

	/** The directory files will be written into */
	private String currentFilepath;
	/** The template used to build the full file name if filewriter is using lazy open */
	private String currentFileFormat;
	/** The currently set value of the next number field on the file writer plugin */
	private Integer currentNextNumber;
	/** The current filename (minus the path/file number) */
	private String currentFilename;

	/**
	 * If the capture PV returns true, return the file path returned by the filename PV.
	 * If either PV is not readable or the capturePV is false, return the default title.
	 */
	@Override
	public String get() {
		if (currentFullFilename != null) {
			return currentFullFilename;
		}
		return getDefaultTitle();
	}

	@Override
	public void configure() throws FactoryException {
		if (!isConfigured()) {
			if (capturePV == null) throw new FactoryException("capturePV is required");
			if (fullFilenamePV == null) throw new FactoryException("fullFilenamePV is required");
			if (arrayIdPV == null) throw new FactoryException("arrayIdPV is required");
			if (filenamePV == null) throw new FactoryException("filenamePV is required");
			if (filepathPV == null) throw new FactoryException("filepathPV is required");
			if (fileformatPV == null) throw new FactoryException("fileformatPV is required");
			if (nextNumberPV == null) throw new FactoryException("nextNumberPV is required");
			fullFilename = LazyPVFactory.newStringFromWaveformPV(fullFilenamePV);
			capture = LazyPVFactory.newBooleanFromEnumPV(capturePV);
			PV<Integer> arrayID = LazyPVFactory.newIntegerPV(arrayIdPV);

			filepath = LazyPVFactory.newStringFromWaveformPV(filepathPV);
			filename = LazyPVFactory.newStringFromWaveformPV(filenamePV);
			fileformat = LazyPVFactory.newStringFromWaveformPV(fileformatPV);
			nextNumber = LazyPVFactory.newIntegerPV(nextNumberPV);

			try {
				arrayID.addMonitorListener(this::updateArray);
				capture.addMonitorListener(this::updateCapture);
				fullFilename.addMonitorListener(this::updateFullFilename);

				filepath.addMonitorListener(this::updateFilepath);
				fileformat.addMonitorListener(this::updateFileFormat);
				filename.addMonitorListener(this::updateFilename);
				nextNumber.addMonitorListener(this::updateNextNumber);
			} catch (IOException e) {
				logger.error("Could not monitor PVs", e);
			}

			setConfigured(true);
		}
	}

	/** Update the file writing state */
	private void updateCapture(MonitorEvent me) {
		capturing = capture.extractValueFromDbr(me.getDBR());
		logger.trace("capturing: {}", capturing);
	}

	/** If a collection is made while not writing file, reset filename */
	private void updateArray(@SuppressWarnings("unused") MonitorEvent me) {
		if (!capturing) {
			logger.trace("New image collected while not writing file. Clearing filename");
			currentFullFilename = null;
		}
	}

	/** If the file name is set while file writing is enabled, save the file name */
	private void updateFullFilename(MonitorEvent me) {
		if (capturing) {
			currentFullFilename = fullFilename.extractValueFromDbr(me.getDBR());
			// If file writing is set to lazy open, full file name is empty until first frame arrives
			if (currentFullFilename == null || currentFullFilename.isEmpty()) {
				currentFullFilename = buildFileName();
			}
			currentFullFilename = Paths.get(currentFullFilename.replaceAll("\\\\", "/")).getFileName().toString();
			logger.trace("Set filename to {}", currentFullFilename);
		}
	}

	private String buildFileName() {
		logger.trace("Building file name from template: {}, {}, {}, {}",
				currentFileFormat, currentFilepath, currentFilename, currentNextNumber);
		return String.format(currentFileFormat, currentFilepath, currentFilename, currentNextNumber);
	}

	private void updateFilepath(MonitorEvent me) {
		currentFilepath = filepath.extractValueFromDbr(me.getDBR());
	}

	private void updateFileFormat(MonitorEvent me) {
		currentFileFormat = fileformat.extractValueFromDbr(me.getDBR());
	}

	private void updateFilename(MonitorEvent me) {
		currentFilename = filename.extractValueFromDbr(me.getDBR());
	}

	private void updateNextNumber(MonitorEvent me) {
		currentNextNumber = nextNumber.extractValueFromDbr(me.getDBR());
	}

	public String getCapturePV() {
		return capturePV;
	}

	public void setCapturePV(String capturePV) {
		this.capturePV = capturePV;
	}

	public String getFilenamePV() {
		return filenamePV;
	}

	public void setFilenamePV(String filenamePV) {
		this.filenamePV = filenamePV;
	}

	public String getDefaultTitle() {
		return defaultTitle;
	}

	public void setDefaultTitle(String defaultTitle) {
		this.defaultTitle = defaultTitle;
	}

	public String getArrayIdPV() {
		return arrayIdPV;
	}

	public void setArrayIdPV(String arrayIdPV) {
		this.arrayIdPV = arrayIdPV;
	}

	public String getFullFilenamePV() {
		return fullFilenamePV;
	}

	public void setFullFilenamePV(String fullFilenamePV) {
		this.fullFilenamePV = fullFilenamePV;
	}

	public String getFilepathPV() {
		return filepathPV;
	}

	public void setFilepathPV(String filepathPV) {
		this.filepathPV = filepathPV;
	}

	public String getNextNumberPV() {
		return nextNumberPV;
	}

	public void setNextNumberPV(String nextNumberPV) {
		this.nextNumberPV = nextNumberPV;
	}

	public String getFileformatPV() {
		return fileformatPV;
	}

	public void setFileformatPV(String fileformatPV) {
		this.fileformatPV = fileformatPV;
	}
}
