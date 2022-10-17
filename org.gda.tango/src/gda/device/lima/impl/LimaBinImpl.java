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

import gda.device.lima.LimaBin;

public class LimaBinImpl implements LimaBin {

	long [] data = new long[]{1,1};
	
	@Override
	public long getBinX() {
		return data[0];
	}

	@Override
	public long getBinY() {
		return data[1];
	}

	@Override
	public void setBinX(long val) {
		data[0] = val;
	}

	@Override
	public void setBinY(long val) {
		data[1] = val;
	}

	@Override
	public String toString() {
		return "LimaBinImpl [getBinX()=" + getBinX() + ", getBinY()=" + getBinY() + "]";
	}
	
}
