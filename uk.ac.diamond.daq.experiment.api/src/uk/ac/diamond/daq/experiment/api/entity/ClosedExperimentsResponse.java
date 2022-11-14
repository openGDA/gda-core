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

package uk.ac.diamond.daq.experiment.api.entity;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * Returns a collection of {@link URL}s pointing to the closed experiments index files
 *
 * @author Maurizio Nagni
 */
@JsonDeserialize(builder = ClosedExperimentsResponse.Builder.class)
public class ClosedExperimentsResponse {

	private final List<URL> indexes;

	public ClosedExperimentsResponse(List<URL> indexes) {
		super();
		this.indexes = indexes;
	}

	/**
	 * The closed experiments index files
	 *
	 * @return a collection of {@link URL}, eventually empty
	 */
	public List<URL> getIndexes() {
		return indexes;
	}

	@JsonPOJOBuilder
	public static class Builder {
		private List<URL> indexes = Collections.emptyList();

		public void withIndexes(List<URL> indexes) {
			this.indexes = Optional.ofNullable(indexes)
					.orElseGet(Collections::emptyList);
		}

	    public ClosedExperimentsResponse build() {
	        return new ClosedExperimentsResponse(indexes);
	    }
	}
}
