/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package gda.device.zebra.controller;

import java.io.Serializable;

/**
 * Indicates that one of the soft inputs has changed.
 */
public class SoftInputChangedEvent implements Serializable {

	private final int inputNumber;

	private final boolean set;

	public SoftInputChangedEvent(int inputNumber, boolean set) {
		this.inputNumber = inputNumber;
		this.set = set;
	}

	public int getInputNumber() {
		return inputNumber;
	}

	public boolean isSet() {
		return set;
	}

	@Override
	public String toString() {
		return String.format("%s[inputNumber=%d, set=%s]", getClass().getName(), inputNumber, set);
	}

}
