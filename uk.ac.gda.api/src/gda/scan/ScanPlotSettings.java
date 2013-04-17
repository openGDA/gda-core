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

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScanPlotSettings implements Serializable {
	
	/**
	 * The following are values for unlistedColumnBehaviour
	 */
	public static int PLOT=0;
	public static int PLOT_NOT_VISIBLE=1;
	public static int IGNORE=2;
	
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(ScanPlotSettings.class);
	private String xAxisName;
	private String[] yAxesShown, yAxesNotShown;
	private Double xMin, xMax;
	private boolean ignore = false;
	private int unlistedColumnBehaviour = IGNORE;
	private boolean allowUseOfPreviousScanSettings = true;
	private Map<String, String> yAxesMap;

	public String getXAxisName() {
		return xAxisName;
	}

	public void setXAxisName(String axisName) {
		xAxisName = axisName;
	}

	public Double getXMin() {
		return xMin;
	}

	public void setXMin(Double min) {
		xMin = min;
	}

	public Double getXMax() {
		return xMax;
	}

	public void setXMax(Double max) {
		xMax = max;
	}

	/**
	 * The list of y-axes to plot and make visible
	 * <p>
	 * If you do not want anything plotted and visible, then give this an empty list.
	 * <p>
	 * If this is null, or not set, then anything outside of the yAxesShown and yAxesNotShown lists will be plotted and
	 * visible.
	 * 
	 * @param yAxesShown
	 */
	public void setYAxesShown(String[] yAxesShown) {
		this.yAxesShown = yAxesShown;
	}

	/**
	 * The list of y-axes to plot but make invisible.
	 * <p>
	 * Give this an empty list to ensure anything outside of the yAxesShown list is not plotted.
	 * <p>
	 * If this is null, or not set, then anything outside of the yAxesShown and yAxesNotShown lists will be plotted and
	 * invisible.
	 * 
	 * @param yAxesNotShown
	 */
	public void setYAxesNotShown(String[] yAxesNotShown) {
		this.yAxesNotShown = yAxesNotShown;
	}

	public String[] getYAxesShown() {
		return yAxesShown;
	}

	public String[] getYAxesNotShown() {
		return yAxesNotShown;
	}

	public boolean isIgnore() {
		return ignore;
	}

	public void setIgnore(boolean ignore) {
		this.ignore = ignore;
	}

	public int getUnlistedColumnBehaviour() {
		return unlistedColumnBehaviour;
	}

	public void setUnlistedColumnBehaviour(int unlistedColumnBehaviour) {
		this.unlistedColumnBehaviour = unlistedColumnBehaviour;
	}

	public boolean isAllowUseOfPreviousScanSettings() {
		return allowUseOfPreviousScanSettings;
	}

	public void setAllowUseOfPreviousScanSettings(boolean allowUseOfPreviousScanSettings) {
		this.allowUseOfPreviousScanSettings = allowUseOfPreviousScanSettings;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (ignore ? 1231 : 1237);
		result = prime * result + unlistedColumnBehaviour;
		result = prime * result + ((xAxisName == null) ? 0 : xAxisName.hashCode());
		result = prime * result + ((xMax == null) ? 0 : xMax.hashCode());
		result = prime * result + ((xMin == null) ? 0 : xMin.hashCode());
		result = prime * result + Arrays.hashCode(yAxesNotShown);
		result = prime * result + Arrays.hashCode(yAxesShown);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ScanPlotSettings other = (ScanPlotSettings) obj;
		if (ignore != other.ignore)
			return false;
		if (unlistedColumnBehaviour != other.unlistedColumnBehaviour)
			return false;
		if (xAxisName == null) {
			if (other.xAxisName != null)
				return false;
		} else if (!xAxisName.equals(other.xAxisName))
			return false;
		if (xMax == null) {
			if (other.xMax != null)
				return false;
		} else if (!xMax.equals(other.xMax))
			return false;
		if (xMin == null) {
			if (other.xMin != null)
				return false;
		} else if (!xMin.equals(other.xMin))
			return false;
		if (!Arrays.equals(yAxesNotShown, other.yAxesNotShown))
			return false;
		if (!Arrays.equals(yAxesShown, other.yAxesShown))
			return false;
		return true;
	}

	private String listToString(String[] list) {
		if (list == null)
			return null;
		String s = "";
		for (String l : list) {
			s += l + ",";
		}
		return s;
	}

	@Override
	public String toString() {
		return "xAxis = " + (xAxisName != null ? xAxisName.toString() : "null") + ", yAxesShown = "
				+ listToString(yAxesShown) + ", yAxesNotShown = " + listToString(yAxesNotShown) + ", xMin = "
				+ (xMin != null ? xMin.toString() : "null") + ", xMax = "
				+ (xMax != null ? xMax.toString() : "null, unlisted columns: " + unlistedColumnBehaviour);
	}



	public Map<String, String> getyAxesMap() {
		return yAxesMap;
	}

	/**
	 * yAxesMap axis name map.
	 * Key - name of y_axis being plotted. To match a value in y_axes
	 * Entry - name of the y axis to use if the default axis is not to be used
	 */
	public void setyAxesMap(Map<String, String> yAxesMap) {
		this.yAxesMap = yAxesMap;
	}

	public void SetyAxesMapToUseSeparateYAxesForAll(){
		yAxesMap = new HashMap<String, String>();
		if( getYAxesShown() != null){
			for( String s : getYAxesShown()){
				yAxesMap.put(s, s);
			}
		}
		if( getYAxesNotShown() != null){
			for( String s : getYAxesNotShown()){
				yAxesMap.put(s, s);
			}
		}
	}
}
