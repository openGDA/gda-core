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

import java.util.List;

import gda.factory.Findable;

/**
 * A DIAD experiment consists of one or more ISegmentLimiters chained together, one active at a time.
 * When active, each one enables and/or disables {@link ITrigger}s
 *
 */
public interface ISegment extends SEVListener, Findable {

	/**
	 * Enables the given trigger when the segment is activated
	 * @param trigger
	 */
	void enable(ITrigger trigger);

	/**
	 * @return list of triggers enabled within this segment
	 */
	List<ITrigger> getTriggers();

	/**
	 * Called by the Plan
	 */
	void activate();

	/**
	 * Abort and deactivate all its triggers
	 */
	void abort();

	boolean isActivated();

}
