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

import java.util.List;
import java.util.Vector;

/**
 *
 */
public class SingleScanLine extends ScanPair {
	private String scanName;
	private String currentFilename;
	/**
	 * @return id of scan from datawriter
	 */
	public String getCurrentFilename() {
		return currentFilename;
	}
	SingleScanLine(String scanIdentifier,String scanName, ScanLine scanLine) {
		super(scanLine);
		this.scanName = scanName;
		this.currentFilename = scanIdentifier;
	}

	@Override
	public String toString() {
		return scanName + " - " + scanLine.name;
	}

	@Override
	public
	String toLabelString(int maxlength){
		String label = toString();
		if (label.length() > maxlength) {
			int lenOfEach = (maxlength-4)/2;
			int lenOfScanName = scanName.length()-lenOfEach;
			int lenOfScanLineName = scanLine.name.length()-lenOfEach;
			label = "..";
			if( lenOfScanName >=0 )
				label += scanName.substring(lenOfScanName) + "..";
			if( lenOfScanLineName >=0 )
				label += scanLine.name.substring(lenOfScanLineName);
		}
		return label;
	}

	@Override
	List<String> getParents() {
		Vector<String> parents = new Vector<String>();
		parents.add(scanName);
		return parents;
	}
	public String getName() {
		return scanName + " - " + scanLine.name;
	}
}
