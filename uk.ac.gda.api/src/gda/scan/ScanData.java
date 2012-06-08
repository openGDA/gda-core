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

/**
 * Holds metadata about a Scan
 */
public class ScanData implements Serializable {
	public String uniqueName;
	public Boolean hasChild;
	public String[] scannableNames, detectorNames, scannableHeader, detectorHeader;
	public Integer numberOfPoints;
	public String instrument;
	public String command;
	public String scanIdentifier;
	public String creatorPanelName;
	public String currentFilename;
	public Integer numberOfChildScans;
	public String xAxis;
	public String[] yAxesShown;
	public String[] yAxesNotShown;
	public ScanPlotSettings scanPlotSettings;
	public String[][] scannableFormats;
	public String[][] detectorFormats;
	public int[] scanDimensions;
	public static String[] emptyStringArray = new String[0];

	public ScanData() {
		// do nothing
	}

	public ScanData(IScanDataPoint point) {
		uniqueName = point.getUniqueName();
		scannableNames = point.getScannableNames().toArray(emptyStringArray);
		detectorNames = point.getDetectorNames().toArray(emptyStringArray);
		scannableHeader = point.getPositionHeader().toArray(emptyStringArray);
		detectorHeader = point.getDetectorHeader().toArray(emptyStringArray);
		hasChild = point.getHasChild();
		scanIdentifier = point.getScanIdentifier();
		creatorPanelName = point.getCreatorPanelName();
		currentFilename = point.getCurrentFilename();
		numberOfChildScans = point.getNumberOfChildScans();
		instrument = point.getInstrument();
		numberOfPoints = point.getNumberOfPoints();
		command = point.getCommand();
		scanPlotSettings = point.getScanPlotSettings();
		scanDimensions = point.getScanDimensions();
		scannableFormats = point.getScannableFormats();
		detectorFormats = point.getDetectorFormats();
	}

	public String getUniqueName() {
		return uniqueName;
	}

}
