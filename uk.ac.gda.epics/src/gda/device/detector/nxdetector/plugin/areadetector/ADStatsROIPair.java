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
import gda.device.detector.NXDetectorData;
import gda.device.detector.areadetector.v18.NDStatsPVs.BasicStat;
import gda.device.detector.areadetector.v18.NDStatsPVs.CentroidStat;
import gda.device.detector.areadetector.v18.NDStatsPVs.Stat;
import gda.device.detector.nxdata.NXDetectorDataAppender;
import gda.device.detector.nxdetector.NXPlugin;
import gda.device.scannable.PositionInputStream;
import gda.device.scannable.PositionInputStreamCombiner;
import gda.scan.ScanInformation;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Enabled if any stats are enabled. If stats are enabled an ROI must have been set. The names of the specidfied roi and
 * stats plugins are ignored in favour of this plugins name (could be revisited--RobW Feb2013).
 */
public class ADStatsROIPair implements NXPlugin, PositionInputStream<NXDetectorDataAppender> {

	final private ADRectangularROIPlugin roiPlugin;

	final private ADTimeSeriesStatsPlugin statsPlugin;

	final private String name;

	private PositionInputStream<List<NXDetectorDataAppender>> combinedInputStream;

	public ADStatsROIPair(String name, ADRectangularROIPlugin roiPlugin, ADTimeSeriesStatsPlugin statsPlugin) {
		super();
		this.name = name;
		this.roiPlugin = roiPlugin;
		this.statsPlugin = statsPlugin;
	}

	public List<BasicStat> getEnabledBasicStats() {
		return statsPlugin.getEnabledBasicStats();
	}

	public void setEnabledBasicStats(List<BasicStat> enabledBasicStats) {
		statsPlugin.setEnabledBasicStats(enabledBasicStats);
	}

	public List<CentroidStat> getEnabledCentroidStats() {
		return statsPlugin.getEnabledCentroidStats();
	}

	public void setEnabledCentroidStats(List<CentroidStat> enabledCentroidStats) {
		statsPlugin.setEnabledCentroidStats(enabledCentroidStats);
	}

	private List<Stat> getEnabledStats() {
		return statsPlugin.getEnabledStats();
	}

	public ADRectangularROI getRoi() {
		return roiPlugin.getRoi();
	}

	public void setRoi(ADRectangularROI roi) {
		roiPlugin.setRoi(roi);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean willRequireCallbacks() {
		// Will only work if both are set
		return ((!getEnabledStats().isEmpty()) && (getRoi()!=null));
	}

	@Override
	public void prepareForCollection(int numberImagesPerCollection, ScanInformation scanInfo) throws Exception {
		if (!getEnabledStats().isEmpty() && (getRoi()==null)) {
			throw new IllegalStateException("Stats where enabled, but no ROI configured");
		}
		if (!getEnabledStats().isEmpty() && (getRoi()!=null)) {
			throw new IllegalStateException("An ROI was configured, but no stats enabled");
		}
		// prepare even if disabled ad preparation will in this case disable he plugins
		roiPlugin.prepareForCollection(numberImagesPerCollection, scanInfo);
		statsPlugin.prepareForCollection(numberImagesPerCollection, scanInfo);
		List<PositionInputStream<NXDetectorDataAppender>> streams = new ArrayList<PositionInputStream<NXDetectorDataAppender>>();
		streams.add(roiPlugin);
		streams.add(statsPlugin);
		combinedInputStream = new PositionInputStreamCombiner<NXDetectorDataAppender>(streams);
	}

	@Override
	public void prepareForLine() throws Exception {
		roiPlugin.prepareForLine();
		statsPlugin.prepareForLine();
	}

	@Override
	public void completeLine() throws Exception {
		roiPlugin.completeLine();
		statsPlugin.completeLine();
	}

	@Override
	public void completeCollection() throws Exception {
		roiPlugin.completeCollection();
		statsPlugin.completeCollection();
	}

	@Override
	public void atCommandFailure() throws Exception {
		roiPlugin.atCommandFailure();
		statsPlugin.atCommandFailure();
	}

	@Override
	public void stop() throws Exception {
		roiPlugin.stop();
		statsPlugin.stop();
	}

	/**
	 * Returns the roii and stats plugins names prefixed by the roi plugins name. (roi plugins names will probably be empty).
	 */
	@Override
	public List<String> getInputStreamNames() {
		List<String> names = new ArrayList<String>();
		for (String statName : roiPlugin.getInputStreamNames()) {
			names.add(roiPlugin.getRoiName() + "_" + statName);
		}
		for (String statName : statsPlugin.getInputStreamNames()) {
			names.add(roiPlugin.getRoiName() + "_" + statName);
		}
		return names;
	}

	@Override
	public List<String> getInputStreamFormats() {
		return statsPlugin.getInputStreamFormats();
	}
	
	@Override
	public List<NXDetectorDataAppender> read(int maxToRead) throws NoSuchElementException, InterruptedException,
			DeviceException {
		List<List<NXDetectorDataAppender>> newAppenderPairs = combinedInputStream.read(maxToRead);
		
		List<NXDetectorDataAppender> appenders = new ArrayList<NXDetectorDataAppender>();
		for (List<NXDetectorDataAppender> appenderPair : newAppenderPairs) {
			appenders.add(new SerialAppender(appenderPair));
		}
		return appenders;
	}

}

class SerialAppender implements NXDetectorDataAppender {
	
	final private List<NXDetectorDataAppender> appenders;

	public SerialAppender(List<NXDetectorDataAppender> appenders) {
		this.appenders = appenders;
	}

	@Override
	public void appendTo(NXDetectorData data, String detectorName) throws DeviceException {
		for (NXDetectorDataAppender appender : appenders) {
			appender.appendTo(data, detectorName);
		}
	}

}
