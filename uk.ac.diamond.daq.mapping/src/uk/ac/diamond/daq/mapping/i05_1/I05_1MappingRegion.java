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

import org.eclipse.scanning.api.annotation.UiLookup;
import org.eclipse.scanning.api.annotation.UiRequired;

public class I05_1MappingRegion {

	private double xStart = 5;
	private double xStop = 10;
	private double yStart = -3;
	private double yStop = 2;
	private String regionShape = "Rectangle";

	@UiRequired
	public double getxStart() {
		return xStart;
	}

	public void setxStart(double xStart) {
		this.xStart = xStart;
	}

	@UiRequired
	public double getxStop() {
		return xStop;
	}

	public void setxStop(double xStop) {
		this.xStop = xStop;
	}

	@UiRequired
	public double getyStart() {
		return yStart;
	}

	public void setyStart(double yStart) {
		this.yStart = yStart;
	}

	@UiRequired
	public double getyStop() {
		return yStop;
	}

	public void setyStop(double yStop) {
		this.yStop = yStop;
	}

	@UiLookup({ "Rectangle", "Circle", "Others???" })
	@UiRequired
	public String getRegionShape() {
		return regionShape;
	}

	public void setRegionShape(String regionShape) {
		this.regionShape = regionShape;
	}

}
