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

package gda.plots;

import gda.plots.Marker;

import java.awt.Color;
import java.util.Vector;

/**
 *
 */
public interface XYDataHandlerLegend {

	/**
	 * @param currentFilename - file contain the scan data
	 * @param topGrouping
	 * @param subGrouping
	 * @param itemName - the y axis label
	 * @param visible
	 * @param id
	 * @param lineNumber
	 * @param color
	 * @param marker
	 * @param onlyOne - only 1 line exists in the scan so do not create a top group
	 * @param xLabel - the x axis label
	 * @param reloadLegendModel - normally the legend model needs to be reload but if you are restoring a whole legend by a 
	 *                            calling addScan many times in a single event just restore after the calls to improve 
	 *                            performance 
	 */
	public void addScan(String currentFilename, String topGrouping, String [] subGrouping, 
			String itemName, boolean visible, String id, int lineNumber,
			Color color, Marker marker, boolean onlyOne, String xLabel, boolean reloadLegendModel);
	
	//needed by ScanPlot
	/**
	 * 
	 */
	public void removeAllItems();
	
	/**
	 * To allow the visibility of the new scan to be the same as the previous scan
	 * @param visibility - used to specify the visibility of the lines for which the names are required
	 * @return list of yLabels of specified visibility in previous scan 
	 */
	Vector<String> getNamesOfLinesInPreviousScan( boolean visibility);
}
