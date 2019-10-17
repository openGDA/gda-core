/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package gda.device.detector.nxdetector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.ContinuousParameters;
import gda.device.DeviceException;
import gda.device.detector.BufferedDetector;
import gda.device.detector.DetectorBase;
import gda.device.detector.NXDetector;
import gda.device.detector.NexusDetector;
import gda.device.detector.areadetector.v17.NDPluginBase;
import gda.device.detector.nxdetector.plugin.areadetector.ADRoiStatsPair;
import gda.epics.LazyPVFactory;
import gda.epics.PV;
import gda.epics.ReadOnlyPV;
import gda.factory.FactoryException;
import gda.jython.InterfaceProvider;
import gda.scan.ScanInformation;

/**
 * A BufferedDetector to collect data from an NXDetector (area detector) in a ContinuousScan.
 * Most of the functions are delegated to the underlying NXDetector; the values returned during a continuous
 * scan (by {@link #readFrames(int, int)}) are determined by the 'additional plugin' chain on the detector
 * and are the same as returned during a step scan. (This detector class also works in a step scan.)
 *
 */
public class BufferedNXDetector extends DetectorBase implements BufferedDetector, NexusDetector {
	private static final Logger logger = LoggerFactory.getLogger(BufferedNXDetector.class);

	private NXDetector detector;
	private NXCollectionStrategyPlugin collectionStrategy;

	/** Base PV name of Stat plugin */
	private String baseStatPvName;

	/** Base PV name of ROI plugin */
	private String baseRoiPvName;

	private String currentPointPvName;

	private String minCallbackTimePvName = NDPluginBase.MinCallbackTime;

	private List<NexusTreeProvider> allFrames = new ArrayList<>();
	private ContinuousParameters parameters;

	private PV<Double> statMinCallbackTimePv;
	private PV<Double> roiMinCallbackTimePv;
	private ReadOnlyPV<Integer> currentNumPointsPv;

	private boolean continuousMode = false;

	@Override
	public void configure() throws FactoryException {
		if (isConfigured()) {
			return;
		}
		setInputNames(new String[] {});

		if (baseStatPvName == null || baseRoiPvName == null) {
			throw new FactoryException("Cannot configure "+getName()+" : 'baseStatPvName' and 'baseRoiPvName' both need to be set");
		}

		setupCurrentNumPointsPv();
		if (currentNumPointsPv == null) {
			throw new FactoryException("Cannot configure "+getName()+" : 'current point' PV could not be set from manually specified value or ADRoiStatsPair plugin");
		}
		statMinCallbackTimePv = LazyPVFactory.newDoublePV(createPvName(baseStatPvName, minCallbackTimePvName));
		roiMinCallbackTimePv = LazyPVFactory.newDoublePV(createPvName(baseRoiPvName, minCallbackTimePvName));

		setConfigured(true);
	}

	private void setupCurrentNumPointsPv() {
		currentNumPointsPv = null;

		// Create the PV from manually specified PV name :
		if (StringUtils.isNotEmpty(currentPointPvName)) {
			logger.warn("Using manually set 'current point' PV : {}", createPvName(baseStatPvName, currentPointPvName));
			currentNumPointsPv = LazyPVFactory.newReadOnlyIntegerPV(createPvName(baseStatPvName, currentPointPvName));
			return;
		}

		// Get the current number of point PV for time series array from stats plugin
		logger.info("Trying to get 'current point' PV from {} ADRoiStatsPair plugin... ", detector.getName());
		Optional<ReadOnlyPV<Integer>> numPointsPv = detector.getAdditionalPluginList().stream().
			filter(plugin -> plugin instanceof ADRoiStatsPair).
			map(plugin -> ((ADRoiStatsPair) plugin).getStatsPlugin().getTSCurrentPointPV()).
			findFirst();

		if (numPointsPv.isPresent()) {
			currentNumPointsPv = numPointsPv.get();
			logger.info("Using PV from stats plugin : {}", currentNumPointsPv.getPvName());
		} else {
			logger.warn("Could not get PV from {}", detector.getName());
		}
	}

	private String createPvName(String basePv, String pvName) {
		String pv = basePv.trim();
		if (!pv.endsWith(":")) {
			pv += ":";
		}
		return pv + pvName;
	}

	@Override
	public void setContinuousParameters(ContinuousParameters parameters) throws DeviceException {
		this.parameters = parameters;
	}

	@Override
	public ContinuousParameters getContinuousParameters() throws DeviceException {
		return parameters;
	}

	@Override
	public void setContinuousMode(boolean on) throws DeviceException {
		continuousMode = on;
		if (continuousMode) {
			prepareForScan();
			detector.collectData();
		}
	}

	@Override
	public boolean isContinuousMode() throws DeviceException {
		return continuousMode;
	}

	private void prepareForScan() throws DeviceException {
		//Detector plugins should already been setup by call to detector.atScanStart() in atScanStart().
		ScanInformation scanInfo = InterfaceProvider.getCurrentScanInformationHolder().getCurrentScanInformation();
		double collectionTime = parameters.getTotalTime()/parameters.getNumberDataPoints();
		int numPoints = parameters.getNumberDataPoints();
		try {
			allFrames.clear();

			// set detector up for multiple frames
			collectionStrategy.prepareForCollection(collectionTime, numPoints, scanInfo);

			// set min callback time to zero so update record updates happen fast
			statMinCallbackTimePv.putWait(0.0);
			roiMinCallbackTimePv.putWait(0.0);
		} catch (Exception e) {
			throw new DeviceException(e);
		}
	}

	@Override
	public Object[] readFrames(int startFrame, int finalFrame) throws DeviceException {
		// Return data from stored cache if available
		if (finalFrame < allFrames.size()) {
			return allFrames.subList(startFrame, finalFrame).toArray(new NexusTreeProvider[] {});
		}

		// Readout and store the frames.
		int numFrames = finalFrame - startFrame + 1;
		NexusTreeProvider[] detectorData = new NexusTreeProvider[numFrames];
		for(int i=0; i<numFrames; i++) {
			try {
				detectorData[i] = detector.getPositionCallable().call();
				if (detectorData[i]!=null) {
					allFrames.add(detectorData[i]);
				}
			} catch (Exception e) {
				throw new DeviceException("Problem during "+getName()+".readFrames("+startFrame+", "+finalFrame+") - "+e.getMessage(), e);
			}
		}
		return detectorData;
	}

	@Override
	public Object[] readAllFrames() throws DeviceException {
		readFrames(0, getNumberFrames()-1);
		return allFrames.toArray(new NexusTreeProvider[] {});
	}

	@Override
	public int getNumberFrames() throws DeviceException {
		try {
			return currentNumPointsPv.get();
		} catch (IOException e) {
			throw new DeviceException(e);
		}
	}

	@Override
	public void clearMemory() throws DeviceException {
		allFrames.clear();
	}

	@Override
	public int maximumReadFrames() throws DeviceException {
		return 100000;
	}

	@Override
	public void collectData() throws DeviceException {
		detector.collectData();
	}

	@Override
	public void prepareForCollection() throws DeviceException {
		detector.prepareForCollection();
	}

	@Override
	public int getStatus() throws DeviceException {
		return detector.getStatus();
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return false;
	}

	@Override
	public NexusTreeProvider readout() throws DeviceException {
		return (NexusTreeProvider) readFrames(allFrames.size() + 1,allFrames.size() + 1)[0];
	}

	@Override
	public void setCollectionTime(double collectionTime) throws DeviceException {
		detector.setCollectionTime(collectionTime);
	}

	@Override
	public double getCollectionTime() throws DeviceException {
		return detector.getCollectionTime();
	}

	@Override
	public void atScanStart() throws DeviceException {
		detector.atScanStart();
	}

	@Override
	public void atScanLineStart() throws DeviceException {
		detector.atScanLineStart();
	}

	@Override
	public void atScanEnd() throws DeviceException {
		detector.atScanEnd();
	}

	@Override
	public void atScanLineEnd() throws DeviceException {
		detector.atScanLineEnd();
	}

	@Override
	public void atCommandFailure() throws DeviceException {
		detector.atCommandFailure();
	}

	@Override
	public void stop() throws DeviceException {
		detector.stop();
	}

	@Override
	public void waitWhileBusy() throws InterruptedException, DeviceException {
		detector.waitWhileBusy();
	}

	public NXDetector getDetector() {
		return detector;
	}

	public void setDetector(NXDetector detector) {
		this.detector = detector;
	}

	@Override
	public String[] getOutputFormat() {
		return detector.getOutputFormat();
	}

	@Override
	public String[] getExtraNames() {
		return detector.getExtraNames();
	}

	public String getBaseStatPvName() {
		return baseStatPvName;
	}

	/** Set base PV name of Stat plugin to use. (e.g. "ws146-AD-SIM-01:STAT:") */
	public void setBaseStatPvName(String baseStatPvName) {
		this.baseStatPvName = baseStatPvName;
	}

	public String getBaseRoiPvName() {
		return baseRoiPvName;
	}

	/** Set base PV name of ROI plugin to use (e.g. "ws146-AD-SIM-01:ROI:")  */
	public void setBaseRoiPvName(String baseRoiPvName) {
		this.baseRoiPvName = baseRoiPvName;
	}


	public NXCollectionStrategyPlugin getCollectionStrategy() {
		return collectionStrategy;
	}

	/**
	 * Set the collection strategy to be used to prepare the detector for a ContinuousScan.
	 * @param collectionStrategy
	 */
	public void setCollectionStrategy(NXCollectionStrategyPlugin collectionStrategy) {
		this.collectionStrategy = collectionStrategy;
	}

	public String getCurrentPointPointPvName() {
		return currentPointPvName;
	}

	/**
	 * Set this to manually set the PV to use to get the current number of points in the time series array.
	 * If not set (or set to empty string), the PV provided by NXDetector ADRoiStatsPair plugin will be used.
	 * (i.e. TSCurrentPoint, or TS:TSCurrentPoint for RHEL7)
	 * @param currentPointPointPvName
	 */
	public void setCurrentPointPointPvName(String currentPointPointPvName) {
		this.currentPointPvName = currentPointPointPvName;
	}
}
