/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package uk.ac.diamond.daq.persistence.bcm;

import gda.device.Scannable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
class ScannableEntry {
	@Id
	@GeneratedValue
	long id;

	String name;
	transient Scannable scannable;

	Double lowerLimit;
	Double upperLimit;

	ScannableEntry() {
	}

	ScannableEntry(String name, Scannable scannable, Double lowerLimit, Double upperLimit) {
		this.name = name;
		this.scannable = scannable;
		this.lowerLimit = lowerLimit;
		this.upperLimit = upperLimit;
	}

	@Override
	public String toString() {
		return "(" + (lowerLimit == null ? "null" : lowerLimit.toString()) + ":"
				+ (upperLimit == null ? "null" : upperLimit.toString()) + ")";

	}
}