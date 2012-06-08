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

package gda.device.epicsdevice;

import gov.aps.jca.dbr.DBR;

import java.io.Serializable;

/**
 * EpicsDBR Class
 */
public class EpicsDBR implements Serializable {
	final static long serialVersionUID = 1;

	/**
	 * 
	 */
	final public int _count;

	/**
	 * 
	 */
	final public Object _value;

	public EpicsDBR(DBR dbr) {
		_count = dbr.getCount();
		_value = dbr.getValue();
	}

	EpicsDBR() {
		_count = 0;
		_value = null;
	}

	EpicsDBR(int count, Object value) // used for DummyEpicsDevice
	{
		_count = count;
		_value = value;
	}

	@Override
	public String toString() {
		return "DBR.count = " + _count + ". DBR.value = " + _toString();
	}

	/**
	 * @return String
	 */
	final public String _toString() {
		String valStr = null;
		if (_value instanceof double[]) {
			valStr = "";
			for (double val : (double[]) _value) {
				valStr += val + ":";
			}
		}
		if (_value instanceof float[]) {
			valStr = "";
			for (float val : (float[]) _value) {
				valStr += val + ":";
			}
		}
		if (_value instanceof short[]) {
			valStr = "";
			for (short val : (short[]) _value) {
				valStr += val + ":";
			}
		}
		if (_value instanceof int[]) {
			valStr = "";
			for (int val : (int[]) _value) {
				valStr += val + ":";
			}
		}
		if (_value instanceof byte[]) {
			valStr = "";
			for (byte val : (byte[]) _value) {
				valStr += val + ":";
			}
		}
		if (_value instanceof String[]) {
			valStr = "";
			for (String val : (String[]) _value) {
				valStr += val + ":";
			}
		}
		if (valStr != null) {
			if (valStr.endsWith(":")) {
				valStr = valStr.substring(0, valStr.length() - 1);
			}
		}
		if (valStr == null)
			valStr = _value.toString();
		return valStr;
	}
}
