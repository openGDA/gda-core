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


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

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
	public final List<ScanDataPlotConfigLine> linesToAdd;

	private String id = "";
	/**
	 *
	 */
	public final ScanPlotSettings scanPlotSettings;
	/**
	 *
	 */
	private int numberOfScannables;
	/**
	 *
	 */
	public final int numberOfChildScans;
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
		numberOfChildScans = point.getNumberOfChildScans();

		this.scanPlotSettings = scanPlotSettings;
		String[] pointyAxesShown = null;
		String[] pointyAxesNotShown = null;
		String settingsXAxisHeader = null;
		if (scanPlotSettings != null) {
			settingsXAxisHeader = scanPlotSettings.getXAxisName();
			pointyAxesShown = scanPlotSettings.getYAxesShown();
			pointyAxesNotShown = scanPlotSettings.getYAxesNotShown();
		}
		if (settingsXAxisHeader == null) {
			xAxisIndex = point.getHasChild() ? numberOfChildScans : 0;
			settingsXAxisHeader = point.getPositionHeader().get(xAxisIndex);
		} else {
			if (settingsXAxisHeader.isEmpty()) {
				id = point.getUniqueName();
				initialDataAsDoubles=null;
				xAxisIndex = 0;
				xAxisHeader = settingsXAxisHeader;
				linesToAdd = null;
				return; // do not plot anything
			}
			xAxisIndex = point.getPositionHeader().indexOf(settingsXAxisHeader);
		}
		linesToAdd = new ArrayList<>();

		final Set<String> yAxesShown = pointyAxesShown == null ? Collections.emptySet() : Set.of(pointyAxesShown);
		final Set<String> yAxesNotShown = pointyAxesNotShown == null ? Collections.emptySet() : Set.of(pointyAxesNotShown);

		int index = 0;
		initialDataAsDoubles = point.getAllValuesAsDoubles();
		if (initialDataAsDoubles[xAxisIndex] != null) {
			for (int j = 0; j < numberOfScannables; j++, index++) {
				addIfWanted(linesToAdd, initialDataAsDoubles[index], yAxesShown, yAxesNotShown, point.getPositionHeader().get(j),
						index, xAxisIndex,this.scanPlotSettings.getUnlistedColumnBehaviour());
			}
			for (int j = 0; j < point.getDetectorHeader().size(); j++, index++) {
				addIfWanted(linesToAdd, initialDataAsDoubles[index], yAxesShown, yAxesNotShown, point.getDetectorHeader().get(j),
						index, xAxisIndex,this.scanPlotSettings.getUnlistedColumnBehaviour());
			}
		} else {
			logger.warn("xAxis is not plottable for scan {}", point.getUniqueName());
			settingsXAxisHeader="";
		}
		xAxisHeader = settingsXAxisHeader;
		id = point.getUniqueName();
	}

	private void addIfWanted(List<ScanDataPlotConfigLine> linesToAdd, Double val, Set<String> yAxesShown,
			Set<String> yAxesNotShown, String name, int index, int xAxisIndex, int defaultBehaviour) {
		// do not add a line if we are unable to convert the string representation to a double
		if (val == null || index == xAxisIndex)
			return;

		if (yAxesShown.contains(name)) {
			linesToAdd.add(new ScanDataPlotConfigLine(index, name, true));
		} else if (yAxesNotShown.contains(name)) {
			linesToAdd.add(new ScanDataPlotConfigLine(index, name, false));
		} else if (defaultBehaviour == ScanPlotSettings.PLOT) {
			linesToAdd.add(new ScanDataPlotConfigLine(index, name, true));
		} else if (defaultBehaviour == ScanPlotSettings.PLOT_NOT_VISIBLE) {
			linesToAdd.add(new ScanDataPlotConfigLine(index, name, false));
		}
	}
}
