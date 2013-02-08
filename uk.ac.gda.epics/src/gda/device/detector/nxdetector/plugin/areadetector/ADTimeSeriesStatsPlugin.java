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
import gda.device.detector.nxdata.NXDetectorDataAppender;
import gda.device.detector.nxdata.NXDetectorDataDoubleAppender;
import gda.device.detector.nxdata.NXDetectorDataNullAppender;
import gda.device.detector.nxdetector.NXPlugin;
import gda.epics.ReadOnlyPV;
import gda.scan.ScanInformation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

public class ADTimeSeriesStatsPlugin implements NXPlugin {

	private final NDStatsPVs statsPVs;

	private final String name;

	private List<BasicStat> enabledBasicStats = Arrays.asList();

	private List<CentroidStat> enabledCentroidStats = Arrays.asList();

	private ScanInformation scanInfo;

	private TimeSeriesInputStreamCollection timeSeriesCollection;

	public ADTimeSeriesStatsPlugin(NDStatsPVs statsPVs, String name) {
		this.statsPVs = statsPVs;
		this.name = name;
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

	public List<Stat> getEnabledStats() {
		List<Stat> enabledStats = new ArrayList<NDStatsPVs.Stat>();
		enabledStats.addAll(getEnabledBasicStats());
		enabledStats.addAll(getEnabledCentroidStats());
		return enabledStats;
	}

	@Override
	public List<NXDetectorDataAppender> read(int maxToRead) throws NoSuchElementException, InterruptedException,
			DeviceException {
		if (getEnabledStats().isEmpty()) {
			NXDetectorDataAppender[] points = new NXDetectorDataAppender[getNumPointsInLine()];
			Arrays.fill(points, new NXDetectorDataNullAppender());
			return Arrays.asList(points);
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
		return !getEnabledStats().isEmpty();
	}

	@Override
	public void prepareForCollection(int numberImagesPerCollection, ScanInformation scanInfo) throws Exception {
		if (scanInfo == null) {
			throw new NullPointerException("scanInfo is required");
		}
		this.scanInfo = scanInfo;
		statsPVs.getPluginBasePVs().getEnableCallbacksPVPair().putCallback(!getEnabledStats().isEmpty());
		statsPVs.getComputeStatistsicsPV().putCallback(!getEnabledBasicStats().isEmpty());
		statsPVs.getComputeCentroidPV().putCallback(!getEnabledCentroidStats().isEmpty());
	}

	@Override
	public void prepareForLine() throws Exception {
		if (timeSeriesCollection != null) {
			throw new IllegalStateException("A collection has already started");
		}
		if (scanInfo == null) {
			throw new IllegalStateException("prepareForLine called before prepareForCollection");
		}

		if (getEnabledStats().isEmpty()) {
			return; // Don't start a collection
		}
		int numPointsToCollect = getNumPointsInLine();
		List<ReadOnlyPV<Double[]>> tsArrayPVList = new ArrayList<ReadOnlyPV<Double[]>>();
		for (Stat stat: getEnabledStats()) {
			tsArrayPVList.add(statsPVs.getTSArrayPV(stat));
		}
		timeSeriesCollection = new TimeSeriesInputStreamCollection(statsPVs.getTSControlPV(), statsPVs.getTSNumPointsPV(), statsPVs.getTSCurrentPointPV(), tsArrayPVList , numPointsToCollect);
	}

	private int getNumPointsInLine() {
		int[] dimensions = scanInfo.getDimensions();
		return dimensions[dimensions.length - 1];
	}

	@Override
	public void completeLine() throws Exception {
		if (timeSeriesCollection != null) {
			try {
				timeSeriesCollection.waitForCompletion();
			} finally {
				timeSeriesCollection = null;
			}
		}
	}

	@Override
	public void completeCollection() throws Exception {
		scanInfo = null;
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
		for (BasicStat stat : getEnabledBasicStats()) {
			names.add(stat.name().toLowerCase());
		}
		for (CentroidStat stat : getEnabledCentroidStats()) {
			names.add(stat.name().toLowerCase());
		}
		return names;
	}

	@Override
	public List<String> getInputStreamFormats() {
		String[] formats = new String[getEnabledStats().size()];
		Arrays.fill(formats, "%f");
		return Arrays.asList(formats);
	}
}


