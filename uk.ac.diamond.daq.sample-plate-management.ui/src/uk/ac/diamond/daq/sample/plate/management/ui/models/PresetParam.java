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

public class PresetParam extends AbstractParam {

	public PresetParam(String name, double value1) {
		this.name = name;
		this.paramValue = new ParamOneDoubleValue(value1);
	}

	public PresetParam(String name, String value1) {
		this.name = name;
		this.paramValue = new ParamOneStringValue(value1);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getParam() {
		return name + paramValue.getValueString();
	}
}
