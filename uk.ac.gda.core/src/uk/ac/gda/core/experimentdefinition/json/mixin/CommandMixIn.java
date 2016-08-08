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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import gda.commandqueue.Command;
import gda.commandqueue.CommandDetails;
import gda.commandqueue.CommandSummary;

/**
 * Jackson Mix-in class for {@link Command}. Specifies which getters and setters
 * should be used to serialise and deserialise the object.
 */
public abstract class CommandMixIn {

	@JsonIgnore abstract String getDescription();

	@JsonIgnore abstract CommandDetails getDetails();

	@JsonProperty abstract Command.STATE getState();

	@JsonProperty abstract void setState(Command.STATE state);

	@JsonIgnore abstract CommandSummary getCommandSummary();

}
