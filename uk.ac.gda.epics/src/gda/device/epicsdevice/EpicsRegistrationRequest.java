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

import java.io.Serializable;

/**
 * EpicsRegistrationRequest Class
 */
public class EpicsRegistrationRequest implements Serializable {
	final static long serialVersionUID = 1;

	/**
	 * 
	 */
	public final String record;
	/**
	 * 
	 */
	public final String field;

	/**
	 * 
	 */
	public final ReturnType returnType;

	/**
	 * 
	 */
	public final String pvName;

	/**
	 * 
	 */
	public final boolean toWrap;

	/**
	 * 
	 */
	public final Double putTimeout;

	/**
	 * @param returnType
	 * @param record
	 * @param field
	 * @param pvName
	 * @param putTimeout
	 * @param toWrap
	 */
	public EpicsRegistrationRequest(ReturnType returnType, String record, String field, String pvName,
			Double putTimeout, boolean toWrap) {
		this.returnType = returnType;
		this.record = record;
		this.field = field;
		this.pvName = pvName;
		this.toWrap = toWrap;
		this.putTimeout = putTimeout;
	}

	@Override
	public int hashCode() {
		return record.hashCode();
	}

	@Override
	public boolean equals(Object _other) {
		if (_other instanceof EpicsRegistrationRequest) {
			EpicsRegistrationRequest other = (EpicsRegistrationRequest) _other;
			return (returnType.equals(other.returnType) && record.equals(other.record) && field.equals(other.field)
					&& pvName.equals(other.pvName) && toWrap == other.toWrap);
		}
		return false;
	}

	/**
	 * @return EpicsRegistrationRequest
	 */
	public EpicsRegistrationRequest removePVName() {
		return new EpicsRegistrationRequest(returnType, record, field, "", putTimeout, toWrap);
	}
}