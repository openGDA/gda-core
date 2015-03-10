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

import gda.device.lima.LimaSavingHeaderDelimiter;

public class LimaSavingHeaderDelimiterImpl implements LimaSavingHeaderDelimiter {

	String [] data= new String[]{"=","\n",";"};

	@Override
	public String getKeyHeaderDelimiter() {
		return data[0];
	}

	@Override
	public String getEntryHeaderDelimiter() {
		return data[1];
	}

	@Override
	public String getImageNumberHeaderDelimiter() {
		return data[2];
	}

	@Override
	public void setKeyHeaderDelimiter(String val) {
		data[0] = val;
	}

	@Override
	public void setEntryHeaderDelimiter(String val) {
		data[1] = val;
	}

	@Override
	public void setImageNumberHeaderDelimiter(String val) {
		data[2] = val;
	}

	@Override
	public String toString() {
		return "LimaSavingHeaderDelimiterImpl [getKeyHeaderDelimiter()=" + getKeyHeaderDelimiter()
				+ ", getEntryHeaderDelimiter()=" + getEntryHeaderDelimiter() + ", getImageNumberHeaderDelimiter()="
				+ getImageNumberHeaderDelimiter() + "]";
	}

}
