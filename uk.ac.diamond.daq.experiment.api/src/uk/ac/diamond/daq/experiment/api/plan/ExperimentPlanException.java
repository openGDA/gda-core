/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.experiment.api.plan;

public class ExperimentPlanException extends RuntimeException {

	public ExperimentPlanException(Exception cause) {
		super(cause);
	}

	public ExperimentPlanException(String message, Exception cause) {
		super(message, cause);
	}

	public ExperimentPlanException(String message) {
		super(message);
	}

	private static final long serialVersionUID = -6595362754972853437L;

}
