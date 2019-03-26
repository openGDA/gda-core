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

package gda.device.detector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.detector.countertimer.TfgScalerWithDarkCurrent;
import gda.device.scannable.ScannableBase;
import gda.device.scannable.ScannableGetPosition;
import gda.device.scannable.ScannableGetPositionWrapper;
import gda.factory.Finder;
import gda.jython.JythonServerFacade;
import gda.jython.JythonStatus;
import gda.observable.IObserver;
import gda.scan.ScanBase;
import uk.ac.gda.api.remoting.ServiceInterface;
import uk.ac.gda.beans.xspress.XspressParameters;
import uk.ac.gda.devices.detector.FluorescenceDetector;
import uk.ac.gda.devices.detector.FluorescenceDetectorParameters;

/**
 * Class to to provide data used for a detector rate view.
 *
 * <li>An instance of this class runs on the server; calls are made to  {@link #collectData(List)}, passing in a list of
 * scannables. This returns a list of positions for each of the scannables and detectors by performing the same sequence
 * of calls to atScanStart, prepareForCollection, etc. as would normally be done during a step scan.
 * <li>Any Xspress detector passed to collectData will be set to return only the FF value and has file writing switched off;
 * Any Tfg scaler scannables do not correct for dark current, and will not show time frame length.
 * These settings are restored at end of collectData.
 * <li>Set {@link #setCollectionAllowed} to False to prevent collection of data. This can be used to be sure that no data is
 * being collected by the detector rate view whilst a scan is running.
 * <li>This class can also be used in a scan to stop detector rate collection :
 * the {@link #atScanStart} sets the collection flag allowed flag to false, and blocks until the current collection has finished.
 *
 */
@ServiceInterface(DetectorMonitorDataProviderInterface.class)
public class DetectorMonitorDataProvider extends ScannableBase implements DetectorMonitorDataProviderInterface {

	protected static final Logger logger = LoggerFactory.getLogger(DetectorMonitorDataProvider.class);

	private volatile boolean collectFrameIsRunning = false;
	private volatile boolean collectionAllowed = true;

	private double collectionTime = 1.0;

	private boolean darkCurrentRequired = false;
	private static final String TFG_TIME_FIELD_NAME = "time";

	private boolean onlyShowFF;
	private boolean showDtValues;
	private boolean writeHdfFile;

	public DetectorMonitorDataProvider() {
		// Set to empty lists to avoid exceptions when formatting the position
		setOutputFormat(new String[]{});
		setInputNames(new String[]{});
	}

	@Override
	public double getCollectionTime() {
		return collectionTime;
	}

	@Override
	public void setCollectionTime(double collectionTime) {
		this.collectionTime = collectionTime;
	}

	/**
	 * Check if {@link #collectData(List)} can run.
	 * @return true if collection not already taking place, collection is allowed and scan/script is not running
	 */
	private boolean collectDataCanRun() {
		if (isScriptOrScanIsRunning()) {
			logger.info("Cannot collect data : script or scan is already running");
			return false;
		} else if (!collectionAllowed || collectFrameIsRunning) {
			logger.info("Cannot collect data : collection already happening = {}, collection allowed = {}", collectFrameIsRunning, collectionAllowed);
			return false;
		} else {
			return true;
		}
	}

	@Override
	public boolean isScriptOrScanIsRunning() {
		return JythonServerFacade.getInstance().getScanStatus() != JythonStatus.IDLE ||
			   JythonServerFacade.getInstance().getScriptStatus() != JythonStatus.IDLE;
	}

	@Override
	public List<String> collectData(List<String> allScannableNames) throws DeviceException, InterruptedException {
		List<String> data = Collections.emptyList();

		if (!collectDataCanRun()) {
			return data;
		}

		try {
			collectFrameIsRunning = true;
			data = collectFrameOfData(allScannableNames, collectionTime);
		} finally {
			collectFrameIsRunning = false;
		}
		return data;
	}

	/**
	 * Collect single frame of data for given list of scannables, return formatted string of readout values.
	 * This is like doing a step scan with a single point and no 'moving parts'.
	 * @param allScannableNames
	 * @param collectionTime
	 * @return data from detector
	 * @throws DeviceException
	 * @throws InterruptedException
	 */
	private List<String> collectFrameOfData(List<String> allScannableNames, double collectionTime) throws DeviceException, InterruptedException {

		List<Scannable> allScannables = getScannables(allScannableNames); // all scannables

		// Make list of just the detectors
		List<Detector> detectors = allScannables.stream()
				.filter(scn -> scn instanceof Detector)
				.map(scn -> (Detector)scn)
				.collect(Collectors.toList());

		try {
			prepareForCollection(allScannables, detectors, collectionTime);

			for (Scannable scannable : allScannables) {
				scannable.atLevelStart();
			}

			// Collect detector data
			for (Detector det : detectors) {
				logger.debug("Calling {}.collectData()", det.getName());
				det.collectData();
			}

			// Wait for completion
			for (Scannable scn : allScannables) {
				scn.waitWhileBusy();
			}

			for (Scannable scn : allScannables) {
				scn.atLevelEnd();
			}

			// readout data from each scannable
			List<String> outputData = new ArrayList<>();
			for (Scannable scn : allScannables) {
				logger.debug("Reading data out data for {}", scn.getName());
				ScannableGetPosition wrapper = new ScannableGetPositionWrapper(scn.getPosition(),
						scn.getOutputFormat());

				List<String> formattedValues = new ArrayList<>();
				formattedValues.addAll(Arrays.asList(wrapper.getStringFormattedValues()));

				// Remove time value from Tfg readout
				if (scn instanceof TfgScalerWithDarkCurrent) {
					String[] extraNames = scn.getExtraNames();
					for(int i=0; i<extraNames.length; i++) {
						if (extraNames[i].equalsIgnoreCase(TFG_TIME_FIELD_NAME)) {
							formattedValues.remove(i);
							break;
						}
					}
				}
				outputData.addAll(formattedValues);
			}
			return outputData;
		} finally {
			restoreDetectorSettings(allScannables);
		}
	}


	private void restoreDetectorSettings(List<Scannable> allScannables) {
		for(Scannable scn : allScannables) {
			restoreTfgSettings(scn);
			restoreFluoDetector(scn);
		}
	}

	/**
	 * Save 'show FF' and 'show deadtime', hdf file writing parameters for FluorescenceDectector
	 * @param scn
	 */
	private void prepareFluoDetector(Scannable scn) {
		if (! (scn instanceof FluorescenceDetector) ) {
			return;
		}
		FluorescenceDetector detector = (FluorescenceDetector)scn;
		FluorescenceDetectorParameters params= detector.getConfigurationParameters();
		if (params instanceof XspressParameters) {
			XspressParameters parameters = (XspressParameters) params;
			onlyShowFF = parameters.isOnlyShowFF();
			showDtValues = parameters.isShowDTRawValues();

			parameters.setOnlyShowFF(true);
			parameters.setShowDTRawValues(false);
		}
		writeHdfFile = detector.isWriteHDF5Files();
		detector.setWriteHDF5Files(false);
	}

	/**
	 * Restore previously saved 'show FF', 'show deadtime', hdf file writing values to FluorescenceDetector
	 * @param scn
	 */
	private void restoreFluoDetector(Scannable scn) {
		if (! (scn instanceof FluorescenceDetector) ) {
			return;
		}
		FluorescenceDetector detector = (FluorescenceDetector)scn;
		FluorescenceDetectorParameters params= detector.getConfigurationParameters();
		if (params instanceof XspressParameters) {
			XspressParameters parameters = (XspressParameters) params;
			parameters.setOnlyShowFF(onlyShowFF);
			parameters.setShowDTRawValues(showDtValues);
		}
		detector.setWriteHDF5Files(writeHdfFile);
	}

	/**
	 * Save currently set value of Tfg darkCurrentRequired and switch off dark current collection.
	 * @param tfg
	 */
	private void prepareTfgSettings(Scannable scn) {
		if (scn instanceof TfgScalerWithDarkCurrent) {
			TfgScalerWithDarkCurrent tfg = (TfgScalerWithDarkCurrent) scn;
			darkCurrentRequired = tfg.isDarkCurrentRequired();
			tfg.setDarkCurrentRequired(false);
		}
	}

	/**
	 * Prepare Tfg for collecting a frame of data - save and switch off dark current
	 * collection and clear any previously set frames.
	 * @param tfg
	 * @throws DeviceException
	 */
	private void prepareTfg(Scannable scn) throws DeviceException {
		if (scn instanceof TfgScalerWithDarkCurrent) {
			TfgScalerWithDarkCurrent tfg = (TfgScalerWithDarkCurrent) scn;
			prepareTfgSettings(tfg);
			tfg.clearFrameSets();
		}
	}

	/**
	 * Restore previously saved values of darkCurrentRequired on the Tfg
	 * @param tfg
	 */
	private void restoreTfgSettings(Scannable scn) {
		if (scn instanceof TfgScalerWithDarkCurrent) {
			TfgScalerWithDarkCurrent tfg = (TfgScalerWithDarkCurrent) scn;
			tfg.setDarkCurrentRequired(darkCurrentRequired);
		}
	}

	/**
	 * Prepare scannables for data collection by performing same steps as {@link ScanBase#prepareDevicesForCollection()};
	 *
	 * @param scannables
	 * @param detectors
	 * @param collectionTime
	 * @throws DeviceException
	 */
	private void prepareForCollection(List<Scannable> scannables, List<Detector> detectors, double collectionTime) throws DeviceException {
		String list = scannables.stream().map(Scannable::getName).collect(Collectors.toList()).toString();
		logger.debug("prepareForCollection started. Scannables = {}", list);
		for(Detector det : detectors) {
			det.setCollectionTime(collectionTime);
			det.prepareForCollection();
			prepareTfg(det);
			prepareFluoDetector(det);
		}

		for(Scannable scn : scannables) {
			scn.atScanStart();
		}
		for(Scannable scn : scannables) {
			scn.atScanLineStart();
		}
		logger.debug("prepareForCollection finished");
	}


	private List<Scannable> getScannables(List<String> scannableNames) {
		List<Scannable> scannables = new ArrayList<>();
		for (String scnName : scannableNames) {
			Optional<Scannable> scannable = Finder.getInstance().findOptional(scnName);
			if (scannable.isPresent()) {
				scannables.add(scannable.get());
			} else {
				logger.warn("Could not find scannable called {} on server", scnName);
			}
		}
		return scannables;
	}

	@Override
	public List<String> getOutputFields(List<String> scannableNames) {
		List<String> columnNames = new ArrayList<>();
		for (Scannable scannable : getScannables(scannableNames)) {
			prepareFluoDetector(scannable);
			prepareTfgSettings(scannable);
			String[] extraNames = scannable.getExtraNames();
			if (extraNames != null && extraNames.length > 0) {
				List<String> namesToAdd = new ArrayList<>();
				for (String name : extraNames) {
					if (!name.equalsIgnoreCase(TFG_TIME_FIELD_NAME) ) {
						namesToAdd.add(name);
					}
				}
				columnNames.addAll(namesToAdd);
			} else {
				columnNames.add(scannable.getName());
			}

			restoreFluoDetector(scannable);
			restoreTfgSettings(scannable);
		}
		return columnNames;
	}

	@Override
	public void addIObserver(IObserver observer) {
	}

	@Override
	public void deleteIObserver(IObserver observer) {
	}

	@Override
	public void deleteIObservers() {
	}

	@Override
	public boolean getCollectionAllowed() {
		return collectionAllowed;
	}

	@Override
	public void setCollectionAllowed(boolean collectionAllowed) {
		this.collectionAllowed = collectionAllowed;
	}

	// ScannableBase overrides
	@Override
	public void asynchronousMoveTo(Object position) {
		return; // do nothing
	}

	@Override
	public Object rawGetPosition() throws DeviceException {
		return null;
	}

	/**
	 * Set collectionAllowed flag to false and block until current collection has finished.
	 */
	@Override
	public void atScanStart() {
		logger.info("Stopping detector rate view collection at scan start");
		collectionAllowed = false;
		waitWhileBusy();
		logger.info("Detector rate view collection stopped at scan start");
	}

	/**
	 * Reset collectionAllowed flag
	 */
	@Override
	public void atScanEnd() {
		stop();
	}

	@Override
	public void atCommandFailure() throws DeviceException {
		stop();
	}

	@Override
	public boolean isBusy() {
		return collectFrameIsRunning;
	}

	@Override
	public void stop() {
		// Stop GUI collection thread after current collection finishes
		if (collectionAllowed) {
			collectionAllowed = false;
			waitWhileBusy();
		}
		// Allow collection again
		collectionAllowed = true;
	}

	/**
	 * Wait for current collection to finish.
	 */
	@Override
	public void waitWhileBusy() {
		logger.debug("waitWhileBusy started");
		try {
			super.waitWhileBusy();
		} catch (DeviceException|InterruptedException e) {
			logger.error("Thread interrupted in waitWhileBusy", e);
		}
		logger.debug("waitWhileBusy finished");
	}

	/** @return true if collection of detector values is currently taking place.
	 * i.e. {@link #collectData(List)} is currently in progress. */
	@Override
	public boolean getCollectionIsRunning() {
		return collectFrameIsRunning;
	}

}
