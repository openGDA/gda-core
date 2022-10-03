/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.sample.plate.management.ui.paramvalues;
public class ParamThreeDoubleValues implements IParamValue {

	double value1;

	double value2;

	double value3;

	public ParamThreeDoubleValues(double value1, double value2, double value3) {
		this.value1 = value1;
		this.value2 = value2;
		this.value3 = value3;
	}

	public double getFirstValue() {
		return value1;
	}

	public double getSecondValue() {
		return value2;
	}

	public double getThirdValue() {
		return value3;
	}

	@Override
	public String getValueString() {
		return " " + String.valueOf(value1)
			 + " " + String.valueOf(value2)
			 + " " + String.valueOf(value3);
	}
}