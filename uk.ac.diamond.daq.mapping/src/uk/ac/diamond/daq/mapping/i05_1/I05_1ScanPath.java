/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.i05_1;

import org.eclipse.scanning.api.annotation.UiComesAfter;
import org.eclipse.scanning.api.annotation.UiLookup;
import org.eclipse.scanning.api.annotation.UiRequired;

public class I05_1ScanPath {

	private String scanPath = "Raster";
	private double xStep = 0.1;
	private double yStep = 0.1;

	@UiRequired
	@UiLookup({ "Raster", "Snake", "Spiral", "More... ??" })
	public String getScanPath() {
		return scanPath;
	}
	public void setScanPath(String scanPath) {
		this.scanPath = scanPath;
	}

	@UiComesAfter({"scanPath"})
	@UiRequired
	public double getxStep() {
		return xStep;
	}
	public void setxStep(double xStep) {
		this.xStep = xStep;
	}

	@UiComesAfter("xStep")
	@UiRequired
	public double getyStep() {
		return yStep;
	}
	public void setyStep(double yStep) {
		this.yStep = yStep;
	}

}
