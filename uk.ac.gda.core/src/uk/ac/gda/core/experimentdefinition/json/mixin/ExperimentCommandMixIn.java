/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.gda.core.experimentdefinition.json.mixin;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import uk.ac.gda.client.experimentdefinition.IExperimentObject;
import uk.ac.gda.client.experimentdefinition.ui.handlers.ExperimentCommand;

/**
 * Jackson mix-in class for {@link ExperimentCommand}s.
 */
public abstract class ExperimentCommandMixIn {

	@SuppressWarnings("unused")
	@JsonCreator public ExperimentCommandMixIn(
			@JsonProperty("experimentObject") IExperimentObject experimentObject,
			@JsonProperty("scriptFile") String scriptFile) {
		// do nothing
	}

	@JsonIgnore abstract String getExperimentObject();

}
