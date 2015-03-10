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

import gda.device.lima.LimaFlip;

public class LimaFlipImpl implements LimaFlip {

	boolean [] data = new boolean[]{false,false};
	

	@Override
	public boolean getFlipX() {
		return data[0];
	}


	@Override
	public boolean getFlipY() {
		return data[1];
	}


	@Override
	public void setFlipX(boolean flipx) {
		data[0] = flipx;
	}


	@Override
	public void setFlipY(boolean flipy) {
		data[0] = flipy;
	}


	@Override
	public String toString() {
		return "LimaFlipImpl [getFlipX()=" + getFlipX() + ", getFlipY()=" + getFlipY() + "]";
	}

}
