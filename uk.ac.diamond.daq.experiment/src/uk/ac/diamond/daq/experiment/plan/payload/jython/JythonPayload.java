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

package uk.ac.diamond.daq.experiment.plan.payload.jython;

import org.python.core.PyFunction;

import uk.ac.diamond.daq.experiment.api.plan.Payload;

public class JythonPayload implements Payload {

	private final PyFunction function;

	public JythonPayload(PyFunction function) {
		this.function = function;
	}

	public PyFunction getFunction() {
		return function;
	}

}
