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
import uk.ac.diamond.daq.util.logging.deprecation.DeprecationLogger;
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

	private static final DeprecationLogger logger = DeprecationLogger.getLogger(ExperimentServiceResponse.class);
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
	@Deprecated(since="GDA 9.23")
	public ExperimentServiceResponse(URL rootNode, ExperimentErrorCode errorCode) {
		super();
		logger.deprecatedMethod("ExperimentServiceResponse(URL, ExperimentErrorCode)", null, "ExperimentServiceResponse(URL)");
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
	@Deprecated(since="GDA 9.23")
	public ExperimentErrorCode getErrorCode() {
		logger.deprecatedMethod("getErrorCode()", null, "a ResponseEntity");
		return errorCode;
	}

	@JsonPOJOBuilder
	public static class Builder {

		private static final DeprecationLogger logger = DeprecationLogger.getLogger(ExperimentServiceResponse.Builder.class);
		private URL rootNode;
		private ExperimentErrorCode errorCode;

		public void withRootNode(URL rootNode) {
			this.rootNode = rootNode;
		}

		/**
		 * @param errorCode
	     * @deprecated this method will be remove in favour of a @code{ResponseEntity<ErrorReport>}
		 */
		@Deprecated(since="GDA 9.23")
		public void withErrorCode(ExperimentErrorCode errorCode) {
			logger.deprecatedMethod("withErrorCode(ExperimentErrorCode)", null, "a ResponseEntity");
			this.errorCode = errorCode;
		}

	    public ExperimentServiceResponse build() {
	        return new ExperimentServiceResponse(rootNode, errorCode);
	    }
	}
}
