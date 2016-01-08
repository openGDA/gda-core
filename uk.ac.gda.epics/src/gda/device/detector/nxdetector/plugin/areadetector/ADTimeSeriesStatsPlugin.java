/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package gda.device.detector.nxdetector.plugin.areadetector;

import gda.device.DeviceException;
import gda.device.detector.areadetector.v18.NDStatsPVs;
import gda.device.detector.areadetector.v18.NDStatsPVs.BasicStat;
import gda.device.detector.areadetector.v18.NDStatsPVs.CentroidStat;
import gda.device.detector.areadetector.v18.NDStatsPVs.Stat;
import gda.device.detector.areadetector.v18.impl.NDStatsPVsImpl;
import gda.device.detector.nxdata.NXDetectorDataAppender;
import gda.device.detector.nxdata.NXDetectorDataDoubleAppender;
import gda.device.detector.nxdata.NXDetectorDataNullAppender;
import gda.device.detector.nxdetector.FrameCountingNXPlugin;
import gda.device.detector.nxdetector.NXPlugin;
import gda.device.detector.nxdetector.roi.RectangularROI;
import gda.device.detector.nxdetector.roi.RectangularROIProvider;
import gda.epics.ReadOnlyPV;
import gda.scan.ScanInformation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

public class ADTimeSeriesStatsPlugin implements NXPlugin, NDPlugin, FrameCountingNXPlugin {

	public static ADTimeSeriesStatsPlugin createFromBasePVName(String pluginName, String basePVName, RectangularROIProvider<Integer> roiProvider) {
		NDStatsPVs statsPVs = NDStatsPVsImpl.createFromBasePVName(basePVName);
		return new ADTimeSeriesStatsPlugin(statsPVs, pluginName, roiProvider);
	}

	private final NDStatsPVs pvs;

	private final String name;

	private final RectangularROIProvider<Integer> roiProvider;

	private List<BasicStat> enabledBasicStats = Arrays.asList();

	private List<CentroidStat> enabledCentroidStats = Arrays.asList();

	private ScanInformation scanInfo;

	private TimeSeriesInputStreamCollection timeSeriesCollection;

	private String inputNdArrayPort;

	private boolean oneTimeSeriesCollectionPerLine = true;

	public ADTimeSeriesStatsPlugin(NDStatsPVs statsPVs, String name, RectangularROIProvider<Integer> roiProvider) {
		this.pvs = statsPVs;
		this.name = name;
		this.roiProvider = roiProvider;
	}

	public boolean isEnabled() {
		try {
			return ((this.roiProvider.getRoi() != null) && (!getEnabledStats().isEmpty()));
		} catch (Exception e) {
			throw new RuntimeException("Problem getting ROI", e);
		}
	}

	public boolean isOneTimeSeriesCollectionPerLine() {
		return oneTimeSeriesCollectionPerLine;
	}
	/**
	 * Perform one time series per collection per line, rather than one per scan. Defaults to true.
	 * @param oneTimeSeriesCollectionPerLine
	 */
	public void setOneTimeSeriesCollectionPerLine(boolean oneTimeSeriesCollectionPerLine) {
		this.oneTimeSeriesCollectionPerLine = oneTimeSeriesCollectionPerLine;
	}

	public List<BasicStat> getEnabledBasicStats() {
		return enabledBasicStats;
	}

	public void setEnabledBasicStats(List<BasicStat> enabledBasicStats) {
		this.enabledBasicStats = enabledBasicStats;
	}

	public List<CentroidStat> getEnabledCentroidStats() {
		return enabledCentroidStats;
	}

	public void setEnabledCentroidStats(List<CentroidStat> enabledCentroidStats) {
		this.enabledCentroidStats = enabledCentroidStats;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @return ndArrayPort name that *will be configured* during prepareforCollection if an roi were configured.
	 */
	@Override
	public String getInputNDArrayPort() {
		return inputNdArrayPort;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param nDArrayPort ndArrayPort name that *will be configured* during prepareforCollection if an roi were configured.
	 */
	@Override
	public void setInputNDArrayPort(String nDArrayPort) {
		this.inputNdArrayPort = nDArrayPort;
	}

	/**
	 * Get the permanent Epics port name.
	 * @return portName
	 * @throws IOException
	 */
	@Override
	public String getPortName() throws IOException {
		return pvs.getPluginBasePVs().getPortNamePV().get();
	}


	public List<Stat> getEnabledStats() {
		List<Stat> enabledStats = new ArrayList<NDStatsPVs.Stat>();
		enabledStats.addAll(getEnabledBasicStats());
		enabledStats.addAll(getEnabledCentroidStats());
		return enabledStats;
	}

	@Override
	public List<NXDetectorDataAppender> read(int maxToRead) throws NoSuchElementException, InterruptedException,
			DeviceException {
		if (!isEnabled()) {
			List<NXDetectorDataAppender> appenders = new ArrayList<NXDetectorDataAppender>();
			appenders.add(new NXDetectorDataNullAppender());
			return appenders;
		}
		return readFromTimeSeries(maxToRead);
	}

	private List<NXDetectorDataAppender> readFromTimeSeries(int maxToRead) throws InterruptedException, DeviceException {
		if (timeSeriesCollection == null) {
			throw new IllegalStateException("A collection has not been started. Call prepareForLine() first.");
		}
		List<String> elementNames = getInputStreamNames();
		List<NXDetectorDataAppender> appenders = new ArrayList<NXDetectorDataAppender>();
		List<List<Double>> readPoints = timeSeriesCollection.read(maxToRead);
		for (List<Double> point : readPoints) {
			appenders.add(new NXDetectorDataDoubleAppender(elementNames, point));
		}
		return appenders;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean willRequireCallbacks() {
		return isEnabled();
	}

	@Override
	public void prepareForCollection(int numberImagesPerCollection, ScanInformation scanInfo) throws Exception {
		if (scanInfo == null) {
			throw new NullPointerException("scanInfo is required");
		}
		this.scanInfo = scanInfo;
		if (isEnabled()) {
			if (getInputNDArrayPort() != null) {
				pvs.getPluginBasePVs().getNDArrayPortPVPair().putWait(getInputNDArrayPort());
			}
			pvs.getTSReadScanPV().putWait(0);
		}
		pvs.getPluginBasePVs().getEnableCallbacksPVPair().putWait(isEnabled());
		pvs.getComputeStatistsicsPVPair().putWait(!getEnabledBasicStats().isEmpty() && isEnabled());
		pvs.getComputeCentroidPVPair().putWait(!getEnabledCentroidStats().isEmpty() && isEnabled());

		if (!isOneTimeSeriesCollectionPerLine()){
			startNewTimeSeriesCollectionIfRequested();
		}
	}

	@Override
	public void prepareForLine() throws Exception {
		if (scanInfo == null) {
			throw new IllegalStateException("prepareForLine called before prepareForCollection");
		}
		if (isOneTimeSeriesCollectionPerLine()){
			startNewTimeSeriesCollectionIfRequested();
		}
	}

	private void startNewTimeSeriesCollectionIfRequested() throws IOException {
		if (timeSeriesCollection != null) {
			throw new IllegalStateException("A collection has already started");
		}

		if ((getEnabledStats().isEmpty()) || (!isEnabled())) {
			return; // Don't start a collection
		}
		int numPointsToCollect;
		if (isOneTimeSeriesCollectionPerLine()) {
			numPointsToCollect = getNumPointsInLine();
		} else {
			numPointsToCollect = getNumPointsInScan();

		}
		List<ReadOnlyPV<Double[]>> tsArrayPVList = new ArrayList<ReadOnlyPV<Double[]>>();
		for (Stat stat: getEnabledStats()) {
			tsArrayPVList.add(pvs.getTSArrayPV(stat));
		}
		timeSeriesCollection = new TimeSeriesInputStreamCollection(pvs.getTSControlPV(), pvs.getTSNumPointsPV(), pvs.getTSCurrentPointPV(), tsArrayPVList , numPointsToCollect);
	}

	private int getNumPointsInLine() {
		int[] dimensions = scanInfo.getDimensions();
		return dimensions[dimensions.length - 1];
	}

	private int getNumPointsInScan() {
		int[] dimensions = scanInfo.getDimensions();
		int numpoints = 1;
		for (int i = 0; i < dimensions.length; i++) {
			numpoints = numpoints * dimensions[i];
		}
		return numpoints;
	}

	@Override
	public void completeLine(int framesCollected) throws Exception {
		if (isOneTimeSeriesCollectionPerLine()){
			waitForTimeSeriesCollectionCompletion(framesCollected);
		}
	}

	@Override
	public void completeLine() throws Exception {
		completeLine(Integer.MAX_VALUE);
	}

	private void waitForTimeSeriesCollectionCompletion(int framesCollected) throws Exception {
		if (timeSeriesCollection != null) {
			try {
				timeSeriesCollection.waitForCompletion(framesCollected);
				timeSeriesCollection.stop();
			} finally {
				timeSeriesCollection = null;
			}
		}
	}

	@Override
	public void completeCollection(int framesCollected) throws Exception {
		scanInfo = null;
		if (!isOneTimeSeriesCollectionPerLine()){
			waitForTimeSeriesCollectionCompletion(framesCollected);
		}
	}

	@Override
	public void completeCollection() throws Exception {
		completeCollection(Integer.MAX_VALUE);
	}

	@Override
	public void atCommandFailure() throws Exception {
		stop();
	}

	@Override
	public void stop() throws Exception {
		if (timeSeriesCollection != null) {
			timeSeriesCollection.stop();
		}
		scanInfo = null;
		timeSeriesCollection = null;
	}

	@Override
	public List<String> getInputStreamNames() {
		List<String> names = new ArrayList<String>();
		if (isEnabled()) {
			for (BasicStat stat : getEnabledBasicStats()) {
				names.add(getInputStreamNamesPrefix() + stat.name().toLowerCase());
			}
			for (CentroidStat stat : getEnabledCentroidStats()) {
				names.add(getInputStreamNamesPrefix() + stat.name().toLowerCase());
			}
		}
		return names;
	}

	@Override
	public List<String> getInputStreamFormats() {
		if (isEnabled()) {
			String[] formats = new String[getEnabledStats().size()];
			Arrays.fill(formats, "%f");
			return Arrays.asList(formats);
		}
		return Arrays.asList();
	}

	public String getInputStreamNamesPrefix() {
		if (roiProvider == null) {
			return "";
		}
		RectangularROI<Integer> roi;
		try {
			roi = roiProvider.getRoi();
		} catch (Exception e) {
			throw new RuntimeException("Problem getting ROI", e);
		}
		return (roi == null) ? "" : roi.getName().replace(" ", "") + "_";
	}
}


