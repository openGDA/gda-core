/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.api;

import java.util.concurrent.Future;

import uk.ac.diamond.daq.concurrent.Async;
import uk.ac.diamond.daq.mapping.api.document.scanpath.PathInfo;

/**
 * Functional interface for calculating a {@link PathInfo} object, which contains
 * useful preview information about a scan
 */
public interface IPathInfoCalculator<R> {
	/**
	 * Given a request, calculates and returns a
	 * {@link PathInfo} object.
	 * @param request Object containing the model(s) detailing the scan and any extra
	 *  			  constraints on the calculation (such as limiting the number of
	 *  			  points)
	 * @return A new {@link PathInfo} document
	 * @throws PathInfoCalculationException If an issue occurs during calculation
	 */
	PathInfo calculatePathInfo(
			R request) throws PathInfoCalculationException;

	/**
	 * Helper method for calculating {@link PathInfo} with the GDA {@link Async}
	 * framework. As {@link PathInfo} calculation can be a long-running operation
	 * @param request Object containing the model(s) detailing the scan and any extra
	 *  			  constraints on the calculation (such as limiting the number of
	 *  			  points)
	 * @return A future that will yield a {@link PathInfo} object
	 */
	default Future<PathInfo> calculatePathInfoAsync(R request) {
		return Async.submit(() -> calculatePathInfo(request));
	}
}
