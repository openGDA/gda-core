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

public class ScanLine {
	String name;
	private String id;
	boolean visible;
	int line;
	public Color lineColor;
	Marker marker;
	private final String xAxisHeader;

	public ScanLine(String name, boolean visible, String id, int line, Color lineColor, Marker marker, String xAxisHeader) {
		this.name = name;
		this.visible = visible;
		this.line = line;
		this.id = id;
		this.lineColor = lineColor;
		this.xAxisHeader = xAxisHeader;
		this.marker = marker != null ? marker : Marker.CROSS;
	}

	ScanLine copy(){
		return new ScanLine(name, visible, id, line, lineColor, marker, xAxisHeader);
	}
}
