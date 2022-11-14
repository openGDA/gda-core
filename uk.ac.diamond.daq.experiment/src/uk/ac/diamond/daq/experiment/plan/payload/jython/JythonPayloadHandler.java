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
import org.springframework.stereotype.Component;

import uk.ac.diamond.daq.experiment.api.plan.PayloadHandler;

@Component
public class JythonPayloadHandler implements PayloadHandler<JythonPayload> {

	@Override
	public Class<?> getSourceClass() {
		return PyFunction.class;
	}

	@Override
	public Class<JythonPayload> getTargetClass() {
		return JythonPayload.class;
	}

	@Override
	public JythonPayload wrap(Object rawPayload) {
		return new JythonPayload((PyFunction) rawPayload);
	}

	@Override
	public Object handle(JythonPayload payload) {
		return payload.getFunction().__call__();
	}

}
