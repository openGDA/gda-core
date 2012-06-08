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

import java.awt.Color;

/**
* represents a line
*/
public class ScanPair extends SelectableNode {

	ScanLine scanLine;

	ScanPair(ScanLine scanLine) {
		this.scanLine = scanLine;
	}

	@Override
	public String toString() {
		return scanLine.name;
	}

	@Override
	public String toLabelString(int maxlength){
		String text = toString();
		if (text.length() > maxlength) {
			text = "." + text.substring(text.length() - maxlength + 1);
		}	
		return text;
	}
	@Override
	public Selected getSelected() {
		return getSelectedFlag() ? Selected.All : Selected.None;
	}

	@Override
	public void setSelectedFlag(boolean visibleFLag) {
		scanLine.visible = visibleFLag;
	}

	@Override
	public boolean getSelectedFlag() {
		return scanLine.visible;
	}
	@Override
	public Color getColor() {
		return scanLine.lineColor;
	}
	
	/**
	 * @return the id of the line
	 */
	public int getLineNumber(){
		return scanLine.line;
	}
	
	public ScanLine getScanLineCopy(){
		return scanLine.copy();
	}
}


