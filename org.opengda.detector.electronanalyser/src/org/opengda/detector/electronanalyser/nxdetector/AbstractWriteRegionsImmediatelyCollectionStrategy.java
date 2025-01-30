/*-
 * Copyright © 2024 Diamond Light Source Ltd.
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

package org.opengda.detector.electronanalyser.nxdetector;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.builder.NexusObjectWrapper;
import org.opengda.detector.electronanalyser.event.ScanEndEvent;
import org.opengda.detector.electronanalyser.nxdata.NXDetectorDataAnalyserRegionAppender;
import org.opengda.detector.electronanalyser.utils.AnalyserExtraRegionPrinterUtil;
import org.opengda.detector.electronanalyser.utils.NXdetectorAndSliceIteratorStorage;

import gda.configuration.properties.LocalProperties;
import gda.data.scan.datawriter.NexusScanDataWriter;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.detector.nxdata.NXDetectorDataAppender;
import gda.device.detector.nxdetector.AsyncNXCollectionStrategy;
import gda.jython.scriptcontroller.ScriptControllerBase;
import gda.jython.scriptcontroller.Scriptcontroller;
import gda.scan.ScanInformation;

/**
 * Abstract detector collection strategy that allows for writing electron analyser region data immediately.
 * Only compatible with property {@link NexusScanDataWriter#PROPERTY_NAME_CREATE_FILE_AT_SCAN_START} set to true.
 * All other detectors that take part in a scan with this property must also be compatible.
 *
 * @author Oli Wenman
 */
public abstract class AbstractWriteRegionsImmediatelyCollectionStrategy<T> implements AsyncNXCollectionStrategy{

	private static final String REGION_OUTPUT_FORMAT = "%.5E";

	//Spring beans
	private String name;
	private boolean extraRegionPrinting = true;
	private Scriptcontroller scriptcontroller;

	private boolean busy;
	private double[] intensityValues;
	private int regionIndex = 0;
	private Future<?> result;
	private ExecutorService executorService = Executors.newSingleThreadExecutor();
	private final AnalyserExtraRegionPrinterUtil regionPrinter = new AnalyserExtraRegionPrinterUtil();
	private final NXdetectorAndSliceIteratorStorage dataStorage = new NXdetectorAndSliceIteratorStorage();

	private boolean stopAfterCurrentRegion = false;

	public List<NexusObjectProvider<? extends NXobject>> getNexusProviders(final NexusScanInfo info) throws NexusException {
		getDataStorage().getDetectorMap().clear();
		getDataStorage().getSliceIteratorMap().clear();

		final List<NexusObjectProvider<? extends NXobject>> nexusProviders = new ArrayList<>();
		for (T region : getEnabledRegions()) {
			//Create region data
			final NexusObjectWrapper<NXdetector> nexusWrapper = initialiseNXdetectorRegion(region, NexusNodeFactory.createNXdetector(), info);
			setupAxisFields(region, nexusWrapper, info.getOverallRank());
			nexusProviders.add(nexusWrapper);
			//Save in map so data can be written to later
			dataStorage.getDetectorMap().put(nexusWrapper.getName(), nexusWrapper.getNexusObject());
		}

		final NexusObjectWrapper<NXdetector> additionalNexusWrapper = initialiseAdditionalNXdetectorData(NexusNodeFactory.createNXdetector(), info);
		if(additionalNexusWrapper != null) {
			nexusProviders.add(additionalNexusWrapper);
			dataStorage.getDetectorMap().put(additionalNexusWrapper.getName(), additionalNexusWrapper.getNexusObject());
		}
		return nexusProviders;
	}

	/**
	 * Initialise the datasets for a region before the scan starts for you to later write/append to.
	 * @param region object containing information about the datasets to initialise.
	 * @param detector will be used to initialise and store the datasets.
	 * @param info contains useful information about the scan to setup dimensions correctly.
	 */
	protected abstract NexusObjectWrapper<NXdetector> initialiseNXdetectorRegion(final T region, final NXdetector detector, final NexusScanInfo info)  throws NexusException;

	/**
	 * Initialise any extra datasets that are needed for a detector before the scan starts for you to later write/append to.
	 * @param detector will be used to initialise and store the datasets.
	 * @param info contains useful information about the scan to setup dimensions correctly.
	 */
	protected abstract NexusObjectWrapper<NXdetector> initialiseAdditionalNXdetectorData(final NXdetector detector, final NexusScanInfo info)  throws NexusException;

	/**
	 * Setup the axis fields for the datasets that will be added to NXData groups. Come under <dataset_name>_indicies.
	 * Important so that datasets correctly plot and won't crop an axis.
	 * @param nexusWrapper to add the axis fields too
	 * @param scanRank information about the scan dimensions
	 */
	protected abstract void setupAxisFields(final T region, final NexusObjectWrapper<NXdetector> nexusWrapper, final int scanRank);

	protected abstract int calculateAngleAxisSize(T region) throws Exception;

	/**
	 * Method to calculate/get expected number of points in array - varies with detectors!
	 * @param region
	 * @return
	 */
	protected abstract int calculateEnergyAxisSize(T region) throws Exception;

	@Override
	public void prepareForCollection(int numberImagesPerCollection, ScanInformation scanInfo) throws Exception {
		executorService = Executors.newSingleThreadExecutor();
		setRegionIndex(0);
		intensityValues = new double[getEnabledRegions().size()];
		if(isExtraRegionPrinting()) {
			regionPrinter.atScanStart(getEnabledRegionNames(), getInputStreamFormats().toArray(String[]::new));
		}
	}

	protected void beforeCollectData() throws DeviceException {
		if (!LocalProperties.check(NexusScanDataWriter.PROPERTY_NAME_CREATE_FILE_AT_SCAN_START, false)) {
			throw new DeviceException(
				getName() + " must have property '"
				+ NexusScanDataWriter.PROPERTY_NAME_CREATE_FILE_AT_SCAN_START + "' set to true."
			);
		}
		setStatus(Detector.BUSY);
	}

	@Override
	public void collectData() throws DeviceException {
		beforeCollectData();
		setRegionIndex(0);
		Callable<double[]> analyserJob = () -> {
			try {
				collectAllRegionData(getEnabledRegions());
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				handleCollectDataInterrupted();
			}
			finally {
				handleCleanupAfterCollectData();
				setStatus(Detector.IDLE);
			}
			return intensityValues;
		};
		result = executorService.submit(analyserJob);
	}

	/**
	 * Allow any cleanup after data collection if it was interrupted
	 */
	protected abstract void handleCollectDataInterrupted() throws DeviceException;

	/**
	 * Allow any cleanup after data collection completed
	 */
	protected abstract void handleCleanupAfterCollectData();

	/**
	 * Loop through regions and perform data collection on each one.
	 */
	private void collectAllRegionData(List<T> regions) throws Exception {
		Arrays.fill(intensityValues, 0);

		for (int i = 0; i < regions.size(); i++) {
			setRegionIndex(i);
			final T currentRegion = regions.get(getRegionIndex());
			regionCollectData(currentRegion);
			intensityValues[regionIndex] = regionSaveData(currentRegion);

			if (isExtraRegionPrinting()) {
				regionPrinter.printExtraRegionProgress(intensityValues);
			}
			if (Thread.interrupted() || stopAfterCurrentRegion) {
				setStopAfterCurrentRegion(false);
				break;
			}
		}
	}

	/**
	 * Perform the data collection for the specific region.
	 * @param region to setup detector with and perform data collection on.
	 */
	protected abstract void regionCollectData(T region) throws Exception;

	/**
	 * Save the data collection for a region.
	 * @param region that is being saved
	 * @return intensity value that region produced.
	 */
	protected abstract double regionSaveData(final T region) throws Exception;

	@Override
	public List<NXDetectorDataAppender> read(int maxToRead) throws NoSuchElementException, InterruptedException, DeviceException {
		return Arrays.asList(
			new NXDetectorDataAnalyserRegionAppender(
				getEnabledRegionNames().toArray(String[]::new),
				intensityValues.clone()
			)
		);
	}

	public boolean isScanFirstRegion() {
		return getRegionIndex() == 0;
	}

	protected abstract boolean isRegionValid(T region);

	public abstract T getCurrentRegion();

	public abstract List<T> getEnabledRegions();

	public abstract List<String> getEnabledRegionNames();

	//update GUI with scan event
	public void updateScriptController(Serializable event) {
		if (getScriptcontroller() instanceof ScriptControllerBase) {
			getScriptcontroller().update(getScriptcontroller(), event);
		}
	}

	public Scriptcontroller getScriptcontroller() {
		return scriptcontroller;
	}

	public void setScriptcontroller(Scriptcontroller scriptcontroller) {
		this.scriptcontroller = scriptcontroller;
	}

	@Override
	public void waitWhileBusy() throws Exception {
		//Block and wait for result to be available. Any errors during data collection
		//can be passed to framework to stop scan and alert user.
		result.get();
		while (isBusy()) {
			Thread.sleep(100);
		}
	}

	@Override
	public void stop() throws DeviceException, InterruptedException {
		if (result != null) {
			result.cancel(true);
		}
		setStatus(Detector.IDLE);
	}

	public void setStopAfterCurrentRegion(boolean value) {
		stopAfterCurrentRegion  = value;
	}

	public boolean getStopAfterCurrentRegion() {
		return stopAfterCurrentRegion;
	}

	public void atScanEnd() {
		updateScriptController(new ScanEndEvent());
		if(isExtraRegionPrinting()) {
			regionPrinter.atScanEnd();
		}
	}

	@Override
	public List<String> getInputStreamNames() {
		return getEnabledRegionNames();
	}

	@Override
	public List<String> getInputStreamFormats() {
		// Return the number of REGION_OUTPUT_FORMAT's equal to number of regions
		return Collections.nCopies(getEnabledRegionNames().size(), REGION_OUTPUT_FORMAT);
	}

	@Override
	public int getStatus() throws DeviceException {
		return busy ? Detector.BUSY : Detector.IDLE;
	}


	protected void setStatus(int status) {
		busy = (status != Detector.IDLE);
	}

	public boolean isBusy() {
		return busy;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getRegionIndex() {
		return regionIndex;
	}

	public void setRegionIndex(int regionIndex) {
		this.regionIndex = regionIndex;
	}

	public boolean isExtraRegionPrinting() {
		return extraRegionPrinting;
	}

	public void setExtraRegionPrinting(boolean extraRegionPrinting) {
		this.extraRegionPrinting = extraRegionPrinting;
	}

	protected NXdetectorAndSliceIteratorStorage getDataStorage() {
		return dataStorage;
	}
}
