/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.device.lima.impl;

import gda.device.lima.LimaROIInt;

public class LimaROIIntImpl implements LimaROIInt {

	int [] data = new int[]{0,0,0,0};
	
	public LimaROIIntImpl() {
		
	}
	
	public LimaROIIntImpl(int i, int j, int k, int l) {
		data=new int[] {i,j,k,l};
	}

	@Override
	public String toString() {
		return "LimaROIIntImpl [getBeginX()=" + getBeginX() + ", getBeginY()=" + getBeginY() + ", getEndX()=" + getEndX()
				+ ", getEndY()=" + getEndY() + "]";
	}

	@Override
	public int getBeginX() {
		return data[0];
	}

	@Override
	public int getBeginY() {
		return data[2];
	}

	@Override
	public int getEndX() {
		return data[1];
	}

	@Override
	public int getEndY() {
		return data[3];
	}

	
	@Override
	public void setEndY(int val) {
		data[3]= val;
	}

	@Override
	public void setBeginX(int val) {
		data[0] = val;
	}

	@Override
	public void setBeginY(int val) {
		data[2]= val;
	}

	@Override
	public void setEndX(int val) {
		data[1]= val;
	}

	
}
