/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package uk.ac.gda.epics.adviewer.views;

public class ADUtils {
	private static final String COLON_REPLACEMENT = "@";
	public static final String PV_TYPE = "pv//";

	public enum ViewType {MPEG, STATS, ARRAY}

	public static String getViewId(ViewType viewType) {
		String viewId="";
		switch (viewType){
		case MPEG:
			viewId = MJPegView.ID;
			break;
		case ARRAY:
			viewId = TwoDArrayView.ID;
			break;
		case STATS:
			viewId = HistogramView.ID;
			break;
		}
		return viewId;
	}

	public static String getPVServiceName(String detectorName, String pvPrefix, String suffixType) {
		return ADUtils.PV_TYPE + detectorName + "//" + pvPrefix.replace(":", COLON_REPLACEMENT) + "//" + suffixType;
	}

	public static String getDetectorNameFromPVServiceName(String pvServiceName) {
		String name = pvServiceName;
		if(pvServiceName.startsWith(PV_TYPE)){
			String[] split = pvServiceName.split(PV_TYPE);
			String[] splitAgain = split[1].split("//");
			if( splitAgain.length < 2)
				throw new IllegalArgumentException("serviceName should be of form pv//detectorName//pvPrefix[//suffixType] actual value is '" + pvServiceName +"'");
			return splitAgain[0];
		}
		return name;
	}

	public static String getSuffixTypeFromPVServiceName(String pvServiceName) {
		String name = pvServiceName;
		if(pvServiceName.startsWith(PV_TYPE)){
			String[] split = pvServiceName.split(PV_TYPE);
			String[] splitAgain = split[1].split("//");
			if( splitAgain.length < 3)
				return "";
			return splitAgain[2];
		}
		return name;
	}

	public static String getPVFromPVServiceName(String pvServiceName) {

		if(!pvServiceName.startsWith(PV_TYPE)){
			throw new IllegalArgumentException("serviceName must be of form " + PV_TYPE + "label//pvPrefix[//suffixType]  actual value is '" + pvServiceName +"'");
		}
		String[] split = pvServiceName.split(PV_TYPE);
		String[] splitAgain = split[1].split("//");
		if( splitAgain.length < 2)
			throw new IllegalArgumentException("serviceName should be of form pv//detectorName//pvPrefix[//suffixType]  actual value is '" + pvServiceName +"'");
		return splitAgain[1].replace(COLON_REPLACEMENT, ":");
	}


}
