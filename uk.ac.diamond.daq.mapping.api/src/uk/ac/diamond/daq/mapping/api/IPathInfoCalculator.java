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
import uk.ac.diamond.daq.mapping.api.document.scanpath.IPathInfo;
import uk.ac.diamond.daq.mapping.api.document.scanpath.IPathInfoRequest;
import uk.ac.diamond.daq.mapping.api.document.scanpath.MappingPathInfo;

/**
 * Functional interface for calculating a {@link MappingPathInfo} object, which contains
 * useful preview information about a scan
 *
 * @param <R> path request type
 * @param <P> path info result type (the result of the calculation)
 */
public interface IPathInfoCalculator<R extends IPathInfoRequest, P extends IPathInfo> {

	/**
	 * Given a request, calculates and returns a
	 * {@link MappingPathInfo} object.
	 * @param request Object containing the model(s) detailing the scan and any extra
	 *  			  constraints on the calculation (such as limiting the number of
	 *  			  points)
	 * @return A new {@link MappingPathInfo} document
	 * @throws PathInfoCalculationException If an issue occurs during calculation
	 */
	P calculatePathInfo(
			R request) throws PathInfoCalculationException;

	/**
	 * Helper method for calculating {@link MappingPathInfo} with the GDA {@link Async}
	 * framework. As {@link MappingPathInfo} calculation can be a long-running operation
	 * @param request Object containing the model(s) detailing the scan and any extra
	 *  			  constraints on the calculation (such as limiting the number of
	 *  			  points)
	 * @return A future that will yield a {@link MappingPathInfo} object
	 */
	default Future<P> calculatePathInfoAsync(R request) {
		return Async.submit(() -> calculatePathInfo(request));
	}

}
