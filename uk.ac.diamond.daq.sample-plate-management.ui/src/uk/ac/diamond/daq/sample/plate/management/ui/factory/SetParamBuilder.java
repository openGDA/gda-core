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

package uk.ac.diamond.daq.sample.plate.management.ui.factory;

import uk.ac.diamond.daq.sample.plate.management.ui.models.AbstractParam;
import uk.ac.diamond.daq.sample.plate.management.ui.models.SetParam;

public class SetParamBuilder implements IParamBuilder {

	@Override
	public AbstractParam build(String name, Double[] values) {
		if (values.length == 1) {
			return new SetParam(name, values[0]);
		} else if (values.length == 3) {
			return new SetParam(name, values[0], values[1], values[2]);
		}

		return null;
	}

	@Override
	public AbstractParam build(String name, String[] values) {
		if (values.length == 1) {
			return new SetParam(name, values[0]);
		}

		return null;
	}
}