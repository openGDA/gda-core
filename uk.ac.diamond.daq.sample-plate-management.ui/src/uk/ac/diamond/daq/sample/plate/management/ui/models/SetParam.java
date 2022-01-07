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

package uk.ac.diamond.daq.sample.plate.management.ui.models;

import uk.ac.diamond.daq.sample.plate.management.ui.paramvalues.ParamOneDoubleValue;
import uk.ac.diamond.daq.sample.plate.management.ui.paramvalues.ParamOneStringValue;
import uk.ac.diamond.daq.sample.plate.management.ui.paramvalues.ParamThreeDoubleValues;

public class SetParam extends AbstractParam {

	public SetParam(String name, double value1) {
		this.name = name;
		this.paramValue = new ParamOneDoubleValue(value1);
	}

	public SetParam(String name, String value1) {
		this.name = name;
		this.paramValue = new ParamOneStringValue(value1);
	}

	public SetParam(String name, double value1, double value2, double value3) {
		this.name = name;
		this.paramValue = new ParamThreeDoubleValues(value1, value2, value3);
	}

	@Override
	public String getName() {
		return name;
	}

	public String getFirstValue() {
		if (paramValue instanceof ParamOneDoubleValue) {
			return String.valueOf(((ParamOneDoubleValue) paramValue).getFirstValue());
		} else if (paramValue instanceof ParamThreeDoubleValues) {
			return String.valueOf(((ParamThreeDoubleValues) paramValue).getFirstValue());
		}

		return null;
	}

	@Override
	public String getParam() {
		return name + paramValue.getValueString();
	}
}