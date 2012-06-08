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

import gda.device.detector.areadetector.AreaDetectorBin;

public class AreaDetectorBinImpl implements AreaDetectorBin{

	private int binX;
	private int binY;
	
	public AreaDetectorBinImpl(int binx, int biny) {
		this.binX = binx;
		this.binY = biny;
	}

	@Override
	public int getBinX() {
		return binX;
	}

	@Override
	public void setBinX(int binX) {
		this.binX = binX;
	}

	@Override
	public int getBinY() {
		return binY;
	}

	@Override
	public void setBinY(int binY) {
		this.binY = binY;
	}
	
	@Override
	public String toString() {
		return "("+binX+","+binY+")";
	}

}
