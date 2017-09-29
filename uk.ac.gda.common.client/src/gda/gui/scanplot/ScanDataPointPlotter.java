/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.eclipse.january.dataset.DoubleDataset;
import org.jfree.data.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.plots.XYDataHandler;
import gda.plots.XYDataHandlerLegend;
import gda.scan.AxisSpec;
import gda.scan.IScanDataPoint;
import gda.scan.IScanStepId;

public class ScanDataPointPlotter {
	private static final Logger logger = LoggerFactory.getLogger(ScanDataPointPlotter.class);
	private Config config;
	private HashMap<String, Integer> sourceToLine = new HashMap<String, Integer>();
	private XYDataHandler plot;
	private XYDataHandlerLegend legendPanel;
	private final String archiveFolder;

	/**
	 * @param plot
	 * @param legendPanel
	 * @param archiveFolder - Folder for contain the archives of the data being plotted
	 */
	public ScanDataPointPlotter(XYDataHandler plot, XYDataHandlerLegend legendPanel, String archiveFolder) {
		this.plot = plot;
		this.legendPanel = legendPanel;
		this.archiveFolder = archiveFolder;
	}

	/**
	 * @param point
	 */
	public synchronized void addData(IScanDataPoint point) {
		Double[] dataPoints;
		if(point.getScanPlotSettings() != null && point.getScanPlotSettings().isIgnore())
			return;
		boolean newConfig = false;
		if (config == null || !config.isValid(point)) {
			Vector<String> namesOfVisibleLinesInPreviousScan = legendPanel.getNamesOfLinesInPreviousScan(true);
			Vector<String> namesOfInVisibleLinesInPreviousScan = legendPanel.getNamesOfLinesInPreviousScan(false);
			config = new Config(config, point, namesOfVisibleLinesInPreviousScan, namesOfInVisibleLinesInPreviousScan);
			newConfig = true;
			if (config.scanPlotSettings != null && config.scanPlotSettings.getXAxisName() != null && config.scanPlotSettings.getXAxisName().isEmpty())
				logger.warn("Unable to find value for XAxis. Data will not be plotted");
			if( config.linesToAdd != null && config.linesToAdd.size()==0){
				logger.warn("Unable to find values to plot.");
			}
		}
		if (config.scanPlotSettings != null && config.scanPlotSettings.getXAxisName() != null && config.scanPlotSettings.getXAxisName().isEmpty())
			return;
		if( config.linesToAdd != null && config.linesToAdd.size()==0){
			return;
		}
		dataPoints = newConfig ? config.initialDataAsDoubles : point.getAllValuesAsDoubles();
		if (dataPoints == null)
			return;
		Double xVal = dataPoints[config.xAxisIndex];
		if (xVal == null)
			return;
		if (newConfig) {
			plot.setXAxisLabel(config.xAxisHeader);
			plot.setYAxisLabel("Various");
			if (config.scanPlotSettings != null) {
				if (config.scanPlotSettings.getXMax() != null && config.scanPlotSettings.getXMin() != null) {
					Range range = new Range(config.scanPlotSettings.getXMin(), config.scanPlotSettings.getXMax());
					plot.setDomainBounds(range);
				}
			}
		}
		String outerScannablePosition = "";
		Vector<String> outerStepIdsStrings = null;
		if (point.getHasChild()) {
			List<IScanStepId> stepIds = point.getStepIds();
			// build outerStepIdsStrings only if stepIds for parents exist.
			if (stepIds != null && stepIds.size() > 1) {
				outerStepIdsStrings = new Vector<String>();
				try {
					for (int i = 0; i < stepIds.size() - 1; i++) {
						IScanStepId stepId = stepIds.get(i);
						if (stepId != null) {
							String stepIdAsString = stepId.asLabel();
							outerScannablePosition += stepIdAsString;
							outerStepIdsStrings.add(stepIdAsString);
						}
					}
				} catch (Exception ex) {
					logger.error("Unable to parse stepIds", ex);
					// fall through so that the old way is used
				}
			}
			if (outerScannablePosition.isEmpty()) {
				for (int i = 0; i < config.numberofChildScans; i++) {
					Double val = dataPoints[i];
					outerScannablePosition += val != null ? String.valueOf(val) : "?";
				}
			}

		}
		// first work out if we have seen this line before
		String sourceToLineKey = point.getUniqueName() + outerScannablePosition;
		if (sourceToLine.containsKey(sourceToLineKey)) {
			addNewPoints(sourceToLine.get(sourceToLineKey), config.linesToAdd, xVal, dataPoints);
		}
		else {
			try {
				plot.archive(false, archiveFolder);
			} catch (IOException e) {
				logger.warn("Unable to archive plot data", e);
			}
			int newLineNumber = createNewLines(point.getScanIdentifier(), point.getCurrentFilename(),
					outerStepIdsStrings, config.linesToAdd, xVal, dataPoints, false, config.xAxisHeader, true);
			sourceToLine.put(sourceToLineKey, new Integer(newLineNumber));
		}
	}

	void dispose() {
		config = null;
	}

	public synchronized void clearGraph() {
		sourceToLine.clear();
	}

	/*
	 * This works on the assumption that each source (scan) will always produce data sets with the same number of data
	 * points. So the same number of lines are affected each time this is called for a specific scan. @param data Vector
	 * @param scan ScanBase
	 */
	private void addNewPoints(int lineNumber, List<ConfigLine> linesToAdd, Double xVal, Double[] dataPoints) {
		for (ConfigLine line : linesToAdd) {
			Double val = dataPoints[line.indexToData];
			if (val != null) {
				plot.addPointToLine(lineNumber, xVal, val);
			}
			lineNumber++;
		}
	}

	/**
	 * On the assumptions that all data from a single scan will always be in the same format, this method creates new
	 * lines on the graph and makes references in the vectors and hashmaps. It then adds the first data points to those
	 * lines.
	 * @param xAxisHeader
	 *
	 */
	private int createNewLines(int scanIdentifier, String currentFilename,
			Vector<String> stepIdsStrings, List<ConfigLine> linesToAdd, Double xVal,
			Double[] dataPoints, boolean makeGroupAlways, String xAxisHeader, boolean reload) {

		int firstNewLineNumber = plot.getNextAvailableLine();
		int lineNumber = firstNewLineNumber;
		String topGrouping = "Scan:" + scanIdentifier;
		String[] subGrouping = stepIdsStrings != null ? stepIdsStrings.toArray(new String[0]) : new String[0];

		/*
		 * if points has children: a. generate nameValuePairs for other scannables - in scannableNameValuePairs b. for
		 * each scannable in the point but for the x axis scannable b1. create new visible line with headerName set to
		 * header for the point + scan identifier (filename) b2. add a checkbox with label set to the same as b1 to the
		 * list of checkboxes c. for all monitors ( these are after the scannables): c1. create new line with headerName
		 * set to header for the point + scan identifier (filename). Only make the first one visible d. for all
		 * detectors ( these are after the scannables): d1. create new line with headerName set to header for the point
		 * + scan identifier (filename). Only make the first one visible
		 */
		boolean onlyOne = !makeGroupAlways && linesToAdd.size() == 1;
		for (ConfigLine line : linesToAdd) {
			createLineAndCheckBox(currentFilename, lineNumber, topGrouping, subGrouping, line.label,
					line.visible, onlyOne, xAxisHeader, line.yaxisSpec, reload);
			lineNumber++;
		}
		if( xVal != null)
			addNewPoints(firstNewLineNumber, linesToAdd, xVal, dataPoints);
		return firstNewLineNumber;
	}

	private void createLineAndCheckBox(String currentFilename, int lineNumber, String topGrouping,
			String[] subGrouping, String itemName, boolean visible, boolean onlyOne, String xAxisHeader, AxisSpec yAxisSpec, boolean reloadLegendModel) {
		String subGroupLabel="";
		for( String s : subGrouping){
			subGroupLabel += "," + s;
		}
		String id = topGrouping + " " + subGroupLabel + " " + itemName;
		plot.initializeLine(lineNumber, XYDataHandler.LEFTYAXIS, id, xAxisHeader, itemName, currentFilename, yAxisSpec);
		legendPanel.addScan(currentFilename, topGrouping, subGrouping, itemName, visible, id, lineNumber, plot
				.getLineColor(lineNumber), plot.getLineMarker(lineNumber), onlyOne, xAxisHeader, reloadLegendModel);
		plot.setLineVisibility(lineNumber, visible);
	}

	public int addData(int scanIdentifier, String currentFileName,
			Vector<String> stepIdsStrings, DoubleDataset xData, DoubleDataset yData, String xAxisHeader, String yAxisHeader, boolean visible, boolean reload, AxisSpec yAxisSpec) {
		String sourceToLineKey = scanIdentifier + yAxisHeader;
		Vector<ConfigLine> linesToAdd = new Vector<ConfigLine>();
		linesToAdd.add(new ConfigLine(0, yAxisHeader,visible, yAxisSpec));
		int newLineNumber = createNewLines(scanIdentifier, currentFileName, stepIdsStrings, linesToAdd, null, null, true,xAxisHeader, reload);
		plot.setsPointsForLine(newLineNumber, xData, yData);
		sourceToLine.put(sourceToLineKey, new Integer(newLineNumber));
		return newLineNumber;
	}

}
