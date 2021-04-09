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

package uk.ac.gda.api.acquisition.configuration.processing;

import java.util.List;

/**
 * Defines builder methods for classes extending {@link ProcessingRequestPair}.
 *
 * <p>
 * Extracting the builder allows to create collections of builders of classes extending {@code ProcessingRequestPair}.
 * (see uk.ac.gda.ui.tool.processing.ProcessingRequestKey)
 * </p>
 *
 * @param <T>
 *
 * @author Maurizio Nagni
 */
public interface ProcessingRequestBuilder<T> {
	/**
	 * In a {@code ProcessingRequestPair} represents the marker of a specific processing request
	 *
	 * @param key a string identifying the process request
	 *
	 * @return a builder with the given key
	 */
	public ProcessingRequestBuilder<T> withKey(String key);

	/**
	 * In a {@code ProcessingRequestPair} represents the marker of a specific processing request
	 *
	 * @param processingFiles a collection of objects associates with the process request
	 *
	 * @return a builder with the given collection of objects
	 */
	public ProcessingRequestBuilder<T> withValue(List<T> processingFiles);

	/**
	 * Build a {@code ProcessingRequestPair}
	 * @return a processing request pair
	 */
	public ProcessingRequestPair<T> build();
}
