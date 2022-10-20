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

import org.springframework.util.StringUtils;

import gda.commandqueue.CommandDetails;
import gda.commandqueue.CommandSummary;
import gda.commandqueue.JythonCommandStringRunnerCommand;
import gda.commandqueue.SimpleCommandDetails;
import gda.commandqueue.SimpleCommandSummary;
import uk.ac.gda.client.experimentdefinition.IExperimentObject;

public class ExperimentCommand extends JythonCommandStringRunnerCommand {

	// inherited object settingsFile not to be used - this class uses the scriptFile.
	protected IExperimentObject experimentObject;
	/**
	 * @param experimentObject
	 *            - the underlying object this will operate
	 * @param commandString
	 *            - the temp file which may be edited to alter the experiment object
	 */
	public ExperimentCommand(IExperimentObject experimentObject, String commandString) {
		this.experimentObject = experimentObject;
		this.commandString = commandString;
	}
	@Override
	public String getDescription() {
		if (experimentObject == null) {
			return super.getDescription();
		}
		return experimentObject.getCommandSummaryString(hasAlreadyBeenRun);
	}
	@Override
	public String toString() {
		return "Experiment Command [commandString=" + commandString + ", description=" + getDescription() + "]";
	}
	@Override
	public CommandSummary getCommandSummary() {
		if (!StringUtils.hasLength(commandString.trim())) {
			return new SimpleCommandSummary(getDescription());
		}
		try {
			experimentObject.parseEditorFile(commandString);
			return new SimpleCommandSummary(getDescription());
		} catch (Exception e) {
			return new SimpleCommandSummary(getDescription());
		}
	}
	@Override
	public CommandDetails getDetails() throws Exception {
		return new SimpleCommandDetails(commandString);
	}
	public IExperimentObject getExperimentObject() {
		return experimentObject;
	}

}
