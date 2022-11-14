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

package uk.ac.diamond.daq.experiment.api.remote;

public interface SEVListenerRequest {


	/**
	 * Name of this request
	 */
	String getName();


	/**
	 * Name of SEV this request will listen to.
	 * {@code null} if signal source is not position.
	 */
	String getSampleEnvironmentVariableName();


	/**
	 * The type of signal produced by the SEV this request listens to
	 */
	SignalSource getSignalSource();

}
