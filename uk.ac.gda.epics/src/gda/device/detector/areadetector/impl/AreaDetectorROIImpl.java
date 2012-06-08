/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.device.detector.areadetector.impl;

import gda.device.detector.areadetector.AreaDetectorROI;


public class AreaDetectorROIImpl implements AreaDetectorROI{
	

	private int minX;
	private int minY;
	private int sizeX;
	private int sizeY;
	
	public AreaDetectorROIImpl(int minx, int miny, int sizex, int sizey) {
		this.minX = minx;
		this.minY = miny;
		this.sizeX = sizex;
		this.sizeY = sizey;
	}
	
	@Override
	public int getMinX() {
		return minX;
	}
	@Override
	public void setMinX(int minX) {
		this.minX = minX;
	}
	@Override
	public int getMinY() {
		return minY;
	}
	@Override
	public void setMinY(int minY) {
		this.minY = minY;
	}
	@Override
	public int getSizeX() {
		return sizeX;
	}
	@Override
	public void setSizeX(int sizeX) {
		this.sizeX = sizeX;
	}
	@Override
	public int getSizeY() {
		return sizeY;
	}
	@Override
	public void setSizeY(int sizeY) {
		this.sizeY = sizeY;
	}
	
	@Override
	public String toString() {
		return "("+minX+","+minY+"),("+sizeX+","+sizeY+")";
	}

}
