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

import java.util.Set;

public interface ISampleEnvironmentVariable {


	/**
	 * Register either an {@link ITrigger} or an {@link ISegment} to this SEV. If it is the first SEVListener, the SEV will begin.
	 * @param listener
	 */
	void addListener(SEVListener listener);

	/**
	 * Deregister an {@link ITrigger}/{@link ISegment} from this SEV. If it is the last SEVListener, the SEV will stop.
	 * @param listener
	 */
	void removeListener(SEVListener listener);

	Set<SEVListener> getListeners();

	boolean isEnabled();

	double read();

	String getName();

}