/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.stage;

import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import uk.ac.diamond.daq.mapping.ui.stage.enumeration.Stage;

/**
 * Describes a stage by its components.
 *
 * @author Maurizio Nagni
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "stage")
@JsonSubTypes({ @Type(value = GTSStage.class, name = "GTS"), @Type(value = TR6Stage.class, name = "TR6") })
public interface StageDescription {

	/**
	 * Returns the described {@link Stage}  type
	 * @return the stage type
	 */
	public Stage getStage();

	/**
	 * Returns a {@link Set} of motors actual positions
	 * @return the motors positions
	 */
	@JsonIgnore()
	public Set<DevicePosition<Double>> getMotorsPosition();

	/**
	 * Any metadata associated with the stage. This definition may change in future.
	 * @return a metadata map
	 */
	public Map<String, String> getMetadata();
}
