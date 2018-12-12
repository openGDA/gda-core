/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.gui.scanplot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.scan.AxisSpec;
import gda.scan.AxisSpecProvider;
import gda.scan.IScanDataPoint;
import gda.scan.ScanPlotSettings;

class Config {
	private static final Logger logger = LoggerFactory.getLogger(Config.class);
	int xAxisIndex;
	String xAxisHeader;
	List<ConfigLine> linesToAdd;
	private String id = "";
	ScanPlotSettings scanPlotSettings;
	private int numberOfScannables;
	private int numberOfDetectors;
	int numberofChildScans;
	Double[] initialDataAsDoubles;
	private AxisSpecProvider yAxesMap;

	boolean isValid(IScanDataPoint pt) {
		return id.equals(pt.getUniqueName());
	}

	private boolean usePreviousScanLineSetting(Config prevConfig, int xAxisIndex, IScanDataPoint point,
			List<String> namesOfVisibleLinesInPreviousScan, List<String> namesOfInVisibleLinesInPreviousScan) {
		if( scanPlotSettings != null && !scanPlotSettings.isAllowUseOfPreviousScanSettings())
			return false;
		if (namesOfVisibleLinesInPreviousScan == null || namesOfVisibleLinesInPreviousScan.isEmpty()
				|| namesOfInVisibleLinesInPreviousScan == null) {
			return false;
		}
		if (prevConfig == null)
			return false;
		if (prevConfig.xAxisIndex != xAxisIndex)
			return false;
		if (prevConfig.xAxisHeader == null)
			return false;
		if (!prevConfig.xAxisHeader.equals(xAxisHeader))
			return false;
		if (prevConfig.numberofChildScans != numberofChildScans)
			return false;
		if (prevConfig.numberOfScannables != numberOfScannables)
			return false;
		if (prevConfig.numberOfDetectors != numberOfDetectors)
			return false;
		if (prevConfig.scanPlotSettings != null && !prevConfig.scanPlotSettings.equals(scanPlotSettings)) {
			return false;
		}
		Set<String> previousScan = new HashSet<>(namesOfVisibleLinesInPreviousScan);
		previousScan.addAll(namesOfInVisibleLinesInPreviousScan);
		Set<String> currentScan = new HashSet<>(point.getPositionHeader());
		currentScan.addAll(point.getDetectorHeader());
		for (String scanName : previousScan) {
			if (!currentScan.contains(scanName)) {
				return false;
			}
		}
		return true;
	}

	Config(Config prevConfig, IScanDataPoint point, List<String> namesOfVisibleLinesInPreviousScan,
			List<String> namesOfInVisibleLinesInPreviousScan) {
		numberOfScannables = point.getPositionHeader().size();
		numberofChildScans = point.getNumberOfChildScans();
		numberOfDetectors = point.getDetectorNames().size();
		scanPlotSettings = point.getScanPlotSettings();
		String[] pointyAxesShown = null;
		String[] pointyAxesNotShown = null;
		if (scanPlotSettings != null) {
			xAxisHeader = scanPlotSettings.getXAxisName();
			yAxesMap = scanPlotSettings.getAxisSpecProvider();
		}

		xAxisIndex = 0;
		if (xAxisHeader == null) {
			if (point.getHasChild()) {
				xAxisIndex = numberofChildScans;
			}
			List<String> positionHeader = point.getPositionHeader();
			if (positionHeader.isEmpty()) {
				id = point.getUniqueName();
				return; // do not plot anything
			}
			xAxisHeader = positionHeader.get(xAxisIndex);
		} else {
			if (xAxisHeader.isEmpty()) {
				id = point.getUniqueName();
				return; // do not plot anything
			}
			xAxisIndex = point.getPositionHeader().indexOf(xAxisHeader);
		}

		if (usePreviousScanLineSetting(prevConfig, xAxisIndex, point, namesOfVisibleLinesInPreviousScan,
				namesOfInVisibleLinesInPreviousScan)) {
			List<String> axesNotToShow = new ArrayList<>();
			for (String s : namesOfInVisibleLinesInPreviousScan) {
				axesNotToShow.add(s);
			}
			if (scanPlotSettings != null) {
				if (scanPlotSettings.getYAxesNotShown() != null) {
					for (String name : scanPlotSettings.getYAxesNotShown()) {
						if (!namesOfVisibleLinesInPreviousScan.contains(name))
							axesNotToShow.add(name);
					}
				}
				if (scanPlotSettings.getYAxesShown() != null) {
					for (String name : scanPlotSettings.getYAxesShown()) {
						if (!namesOfVisibleLinesInPreviousScan.contains(name))
							axesNotToShow.add(name);
					}
				}
			}
			pointyAxesShown = namesOfVisibleLinesInPreviousScan.toArray(new String[] {});
			pointyAxesNotShown = axesNotToShow.toArray(new String[] {});
		} else {
			if (scanPlotSettings != null) {
				pointyAxesShown = scanPlotSettings.getYAxesShown();
				pointyAxesNotShown = scanPlotSettings.getYAxesNotShown();
			}
		}

		linesToAdd = new ArrayList<>();
		int index = 0;
		List<String> yAxesShown = null;
		if (pointyAxesShown != null) {
			yAxesShown = new ArrayList<>();
			for (String yAxis : pointyAxesShown) {
				yAxesShown.add(yAxis);
			}
		}
		List<String> yAxesNotShown = null;
		if (pointyAxesNotShown != null) {
			yAxesNotShown = new ArrayList<>();
			for (String yAxis : pointyAxesNotShown) {
				yAxesNotShown.add(yAxis);
			}
		}

		// scanPlotSettings may be null at this point
		int unlistedBehaviour = ScanPlotSettings.PLOT; // default default is to plot every line
		if (scanPlotSettings != null){
			unlistedBehaviour = scanPlotSettings.getUnlistedColumnBehaviour();
		}

		initialDataAsDoubles = point.getAllValuesAsDoubles();
		if (initialDataAsDoubles[xAxisIndex] != null) {
			for (int j = 0; j < numberOfScannables; j++, index++) {
				addIfWanted(linesToAdd, initialDataAsDoubles[index], yAxesShown, yAxesNotShown, yAxesMap, point
						.getPositionHeader().get(j), index, xAxisIndex, unlistedBehaviour);
			}
			for (int j = 0; j < point.getDetectorHeader().size(); j++, index++) {
				addIfWanted(linesToAdd, initialDataAsDoubles[index], yAxesShown, yAxesNotShown, yAxesMap, point
						.getDetectorHeader().get(j), index, xAxisIndex, unlistedBehaviour);
			}
		} else {
			logger.warn("xAxis is not plottable for scan {}", point.getUniqueName());
			xAxisHeader = "";
		}
		id = point.getUniqueName();
	}

	// FIXME this logic lifted from from ScanDataPointPlotConfig to handle correct behaviour of 'unlisted' columns, but
	// there seems to be some duplication here which needs resolving
	private void addIfWanted(List<ConfigLine> linesToAdd, Double val, List<String> yAxesShown,
			List<String> yAxesNotShown, AxisSpecProvider axisSpecProvider, String name, int index, int xAxisIndex,
			int defaultBehaviour) {
		// do not add a line if we are unable to convert the string representation to a double
		if (val == null)
			return;
		if (index != xAxisIndex) {
			AxisSpec yaxisSpec = axisSpecProvider != null ? axisSpecProvider.getAxisSpec(name) : null;
			if (yAxesShown == null || yAxesShown.contains(name)) {
				linesToAdd.add(new ConfigLine(index, name, true, yaxisSpec));
			} else if (yAxesNotShown == null || yAxesNotShown.contains(name)) {
				linesToAdd.add(new ConfigLine(index, name, false, yaxisSpec));
			} else if (defaultBehaviour == ScanPlotSettings.PLOT) {
				linesToAdd.add(new ConfigLine(index, name, true, yaxisSpec));
			} else if (defaultBehaviour == ScanPlotSettings.PLOT_NOT_VISIBLE) {
				linesToAdd.add(new ConfigLine(index, name, false, yaxisSpec));
			}
		}
	}
}
