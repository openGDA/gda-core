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
import gda.device.detector.areadetector.NDStatsGroup;
import gda.device.detector.areadetector.NDStatsGroupFactory;
import gda.device.detector.areadetector.v17.NDStats;
import gda.device.detector.nxdata.NXDetectorDataAppender;
import gda.device.detector.nxdata.NXDetectorDataDoubleAppender;
import gda.device.detector.nxdetector.NonAsynchronousNXPlugin;
import gda.scan.ScanInformation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ADBasicStats implements NonAsynchronousNXPlugin {

	private static Logger logger = LoggerFactory.getLogger(ADBasicStats.class);

	private final NDStats ndStats;

	private final NDStatsGroup statsGroup;

	private final NDStatsGroup centroidGroup;

	private boolean computeStats = false;

	private boolean computeCentroid = false;

	public ADBasicStats(NDStats ndStats) {
		this.ndStats = ndStats;
		statsGroup = NDStatsGroupFactory.getStatsInstance(getNdStats());
		centroidGroup = NDStatsGroupFactory.getCentroidInstance(getNdStats());
	}

	public NDStats getNdStats() {
		return ndStats;
	}

	public boolean isComputeStats() {
		return computeStats;
	}

	public void setComputeStats(boolean computeStats) {
		this.computeStats = computeStats;
	}

	public boolean isComputeCentroid() {
		return computeCentroid;
	}

	public void setComputeCentroid(boolean computeCentroid) {
		this.computeCentroid = computeCentroid;
	}

	@Override
	public String getName() {
		return "basicstats";
	}

	@Override
	public boolean willRequireCallbacks() {
		return (isComputeCentroid() || isComputeStats());
	}

	@Override
	public void prepareForCollection(int numberImagesPerCollection, ScanInformation scanInfo) throws Exception {
		if (isComputeStats() || isComputeCentroid()) {
			logger.warn("The Stats plugin is not synchronized with putCallBack Acquire");
			logger.warn("This BasicStats plugin is will block the main collection thread");
			getNdStats().getPluginBase().enableCallbacks(); // waits
			getNdStats().getPluginBase().setBlockingCallbacks((short) 1);

		} else {
			getNdStats().getPluginBase().disableCallbacks();
			getNdStats().getPluginBase().setBlockingCallbacks((short) 0);
		}
		getNdStats().setComputeStatistics((short) (isComputeStats() ? 1 : 0)); // TODO: DOES NOT wait
		getNdStats().setComputeCentroid((short) (isComputeCentroid() ? 1 : 0)); // TODO: DOES NOT wait

	}

	@Override
	public void prepareForLine() throws Exception {
	}

	@Override
	public void completeLine() throws Exception {
	}

	@Override
	public void completeCollection() throws Exception {
	}

	@Override
	public void atCommandFailure() throws Exception {
	}

	@Override
	public void stop() throws Exception {
	}

	@Override
	public List<String> getInputStreamNames() {
		List<String> fieldNames = new ArrayList<String>();
		if (isComputeStats()) {
			fieldNames.addAll(Arrays.asList(statsGroup.getFieldNames()));
		}
		if (isComputeCentroid()) {
			fieldNames.addAll(Arrays.asList(centroidGroup.getFieldNames()));
		}
		return fieldNames;
	}

	@Override
	public List<String> getInputStreamFormats() {
		List<String> formats = new ArrayList<String>();
		if (isComputeStats()) {
			formats.addAll(Arrays.asList(statsGroup.getFieldFormats()));
		}
		if (isComputeCentroid()) {
			formats.addAll(Arrays.asList(centroidGroup.getFieldFormats()));
		}
		return formats;
	}

	@Override
	public NXDetectorDataAppender read() throws DeviceException {
		List<Double> values = new ArrayList<Double>();
		if (isComputeStats()) {
			try {
				values.addAll(Arrays.asList(statsGroup.getCurrentDoubleVals()));
			} catch (Exception e) {
				throw new DeviceException(e);
			}
		}
		if (isComputeCentroid()) {
			try {
				values.addAll(Arrays.asList(centroidGroup.getCurrentDoubleVals()));
			} catch (Exception e) {
				throw new DeviceException(e);
			}
		}
		return new NXDetectorDataDoubleAppender(getInputStreamNames(), values);
	}
}
