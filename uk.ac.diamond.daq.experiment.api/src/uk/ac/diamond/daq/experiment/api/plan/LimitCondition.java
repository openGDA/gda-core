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

/**
 * Functional interface passed to SEV-based {@link ISegment}s to specify the limit.
 */
@FunctionalInterface
public interface LimitCondition {

	/**
	 * @param signal the signal to evaluate
	 * @return {@code true} if the signal will terminate the currently active {@link ISegment}; otherwise {@code false}
	 */
	boolean limitReached(double signal);

}
