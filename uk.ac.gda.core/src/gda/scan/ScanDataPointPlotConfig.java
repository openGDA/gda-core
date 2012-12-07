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

package gda.scan;


import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * class that holds information on which data from a scandata point is to be plotted and where to get that data
 */
public class ScanDataPointPlotConfig {
	private static final Logger logger = LoggerFactory.getLogger(ScanDataPointPlotConfig.class);	
	/**
	 * 
	 */
	public final int xAxisIndex;
	/**
	 * 
	 */
	public final String xAxisHeader;
	/**
	 * 
	 */
	public final Vector<ScanDataPlotConfigLine> linesToAdd;
	String id = "";
	/**
	 * 
	 */
	public final ScanPlotSettings scanPlotSettings;
	/**
	 * 
	 */
	int numberOfScannables;
	/**
	 * 
	 */
	public final int numberofChildScans;
	/**
	 * data from point used to construct this config
	 */
	public final Double[] initialDataAsDoubles;	

	/**
	 * @param pt
	 * @return true if the config was constructed for a ScanDataPoint of the same scan as the current point
	 */
	public boolean isValid(ScanDataPoint pt) {
		return id.equals(pt.getUniqueName());
	}

	/**
	 * @param point
	 * @param scanPlotSettings 
	 */
	public ScanDataPointPlotConfig(ScanDataPoint point, ScanPlotSettings scanPlotSettings) {
		numberOfScannables = point.getPositionHeader().size();
		numberofChildScans = point.getNumberOfChildScans();

		this.scanPlotSettings = scanPlotSettings;
		String[] pointyAxesShown = null;
		String[] pointyAxesNotShown = null;
		String settings_xAxisHeader=null;
		if (scanPlotSettings != null) {
			settings_xAxisHeader = scanPlotSettings.getXAxisName();
			pointyAxesShown = scanPlotSettings.getYAxesShown();
			pointyAxesNotShown = scanPlotSettings.getYAxesNotShown();
		}
		if (settings_xAxisHeader == null) {
			if (point.getHasChild()) {
				xAxisIndex = numberofChildScans;
			} else {
				xAxisIndex = 0;
			}
			settings_xAxisHeader = point.getPositionHeader().get(xAxisIndex);
		} else {
			if (settings_xAxisHeader.isEmpty()) {
				id = point.getUniqueName();
				initialDataAsDoubles=null;
				xAxisIndex = 0;
				xAxisHeader = settings_xAxisHeader;
				linesToAdd = null;
				return; // do not plot anything
			}
			xAxisIndex = point.getPositionHeader().indexOf(settings_xAxisHeader);
		}
		linesToAdd = new Vector<ScanDataPlotConfigLine>();
		int index = 0;
		Vector<String> yAxesShown = null;
		if (pointyAxesShown != null) {
			yAxesShown = new Vector<String>();
			for (String yAxis : pointyAxesShown) {
				yAxesShown.add(yAxis);
			}
		}
		Vector<String> yAxesNotShown = null;
		if (pointyAxesNotShown != null) {
			yAxesNotShown = new Vector<String>();
			for (String yAxis : pointyAxesNotShown) {
				yAxesNotShown.add(yAxis);
			}
		}

		initialDataAsDoubles = point.getAllValuesAsDoubles();
		if( initialDataAsDoubles[xAxisIndex] != null){
			for (int j = 0; j < numberOfScannables; j++, index++) {
				addIfWanted(linesToAdd, initialDataAsDoubles[index], yAxesShown, yAxesNotShown, point.getPositionHeader().get(
						j), index, xAxisIndex,this.scanPlotSettings.getUnlistedColumnBehaviour());
			}
			for (int j = 0; j < point.getDetectorHeader().size(); j++, index++) {
				addIfWanted(linesToAdd, initialDataAsDoubles[index], yAxesShown, yAxesNotShown, point.getDetectorHeader().get(
						j), index, xAxisIndex,this.scanPlotSettings.getUnlistedColumnBehaviour());
			}
		} else {
			logger.warn("xAxis is not plottable for scan "+ point.getUniqueName());
			settings_xAxisHeader="";
		}
		xAxisHeader = settings_xAxisHeader;
		id = point.getUniqueName();
	}

	private void addIfWanted(Vector<ScanDataPlotConfigLine> linesToAdd, Double val, Vector<String> yAxesShown,
			Vector<String> yAxesNotShown, String name, int index, int xAxisIndex, ScanPlotSettings.UnlistedColumnBehaviour defaultBehaviour) {
		// do not add a line if we are unable to convert the string representation to a double
		if (val == null)
			return;
		if (index != xAxisIndex) {
			if (yAxesShown == null || yAxesShown.contains(name)) {
				linesToAdd.add(new ScanDataPlotConfigLine(index, name, true));
			} else if (yAxesNotShown == null || yAxesNotShown.contains(name)) {
				linesToAdd.add(new ScanDataPlotConfigLine(index, name, false));
			} else if (defaultBehaviour == ScanPlotSettings.UnlistedColumnBehaviour.PLOT) {
				linesToAdd.add(new ScanDataPlotConfigLine(index, name, true));
			} else if (defaultBehaviour == ScanPlotSettings.UnlistedColumnBehaviour.PLOT_NOT_VISIBLE) {
				linesToAdd.add(new ScanDataPlotConfigLine(index, name, false));
			}
		}
	}
}
