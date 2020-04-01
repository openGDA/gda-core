/*-
 * Copy	right © 2020 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.experiment.api.structure;

import uk.ac.gda.api.exception.GDAException;

public class ExperimentControllerException extends GDAException {

	private static final long serialVersionUID = 2674426638977323584L;

	public ExperimentControllerException() {
		super();
	}

	public ExperimentControllerException(String message) {
		super(message);
	}

	public ExperimentControllerException(Throwable cause) {
		super(cause);
	}

	public ExperimentControllerException(String message, Throwable cause) {
		super(message, cause);
	}

	public ExperimentControllerException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
