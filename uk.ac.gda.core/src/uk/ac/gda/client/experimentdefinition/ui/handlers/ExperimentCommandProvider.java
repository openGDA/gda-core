/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package uk.ac.gda.client.experimentdefinition.ui.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.commandqueue.Command;
import gda.commandqueue.CommandProvider;
import uk.ac.gda.client.experimentdefinition.IExperimentObject;

public class ExperimentCommandProvider implements CommandProvider {

	private final static Logger logger = LoggerFactory.getLogger(ExperimentCommandProvider.class);

	private String commandString;
	private IExperimentObject ob;
	private String description;

	public ExperimentCommandProvider(String commandString, String description) {
		super();
		this.commandString = commandString;
		this.description = description;
	}

	public ExperimentCommandProvider(IExperimentObject ob) {
		super();

		try {
			commandString = ob.getCommandString();
		} catch (Exception e) {
			logger.error(
					"Exception getting command string to enable editing of queued scan. Scan cannot be edited from the queue.",
					e);
		}
		this.ob = ob;
	}

	@Override
	public Command getCommand() {
		ExperimentCommand command = new ExperimentCommand(ob, commandString);
		if (ob == null) {
			command.setDescription(description);
		}
		return command;
	}
}
