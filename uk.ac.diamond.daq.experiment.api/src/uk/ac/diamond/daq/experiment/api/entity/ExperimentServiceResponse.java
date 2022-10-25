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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * Describe to a client the outcome of an experiment service request.
 *
 * @author Maurizio Nagni
 *
 */
@JsonDeserialize(builder = ExperimentServiceResponse.Builder.class)
public class ExperimentServiceResponse {

	private final URL rootNode;

	private ExperimentErrorCode errorCode;

	/**
	 * @param rootNode
	 * @param errorCode
	 *
	 * @deprecated use instead {@link ExperimentServiceResponse#ExperimentServiceResponse(URL)}. Any error should be handled on the rest service level.
	 *
	 * @see @code{ExperimentRestService#exceptionHandler}
	 */
	@Deprecated
	public ExperimentServiceResponse(URL rootNode, ExperimentErrorCode errorCode) {
		super();
		this.rootNode = rootNode;
		this.errorCode = errorCode;
	}

	public ExperimentServiceResponse(URL rootNode) {
		super();
		this.rootNode = rootNode;
	}

	/**
	 * Return the experiment root node
	 *
	 * @return experiment root node.
	 */
	public URL getRootNode() {
		return rootNode;
	}

	/**
	 * @return
	 *
	 * @deprecated this method will be remove in favour of a @code{ResponseEntity<ErrorReport>}
	 *
	 * @see @code{ExperimentRestService#exceptionHandler}
	 */
	@Deprecated
	public ExperimentErrorCode getErrorCode() {
		return errorCode;
	}

	@JsonPOJOBuilder
	public static class Builder {
		private URL rootNode;
		private ExperimentErrorCode errorCode;

		public void withRootNode(URL rootNode) {
			this.rootNode = rootNode;
		}

		/**
		 * @param errorCode
	     * @deprecated this method will be remove in favour of a @code{ResponseEntity<ErrorReport>}
		 */
		@Deprecated
		public void withErrorCode(ExperimentErrorCode errorCode) {
			this.errorCode = errorCode;
		}

	    public ExperimentServiceResponse build() {
	        return new ExperimentServiceResponse(rootNode, errorCode);
	    }
	}
}
