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

import java.io.Serializable;

import org.springframework.util.StringUtils;

import gda.commandqueue.CommandDetails;
import gda.commandqueue.CommandSummary;
import gda.commandqueue.JythonScriptFileRunnerCommand;
import gda.commandqueue.SimpleCommandDetailsPath;
import gda.commandqueue.SimpleCommandSummary;
import uk.ac.gda.client.experimentdefinition.IExperimentObject;

public class ExperimentCommand extends JythonScriptFileRunnerCommand implements Serializable {

	// inherited object settingsFile not to be used - this class uses the scriptFile.

	protected IExperimentObject experimentObject;

	/**
	 * @param experimentObject
	 *            - the underlying object this will operate
	 * @param scriptFile
	 *            - the temp file which may be edited to alter the experiment object
	 */
	public ExperimentCommand(IExperimentObject experimentObject, String scriptFile) {
		this.experimentObject = experimentObject;
		this.scriptFile = scriptFile;
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
		return "Experiment Command [tempFile=" + scriptFile + ", description=" + getDescription() + "]";
	}

	@Override
	public CommandSummary getCommandSummary() {
		if (!StringUtils.hasLength(scriptFile.trim())) {
			return new SimpleCommandSummary(getDescription());
		}
		try {
			experimentObject.parseEditorFile(scriptFile);
			return new SimpleCommandSummary(getDescription());
		} catch (Exception e) {
			return new SimpleCommandSummary(getDescription());
		}
	}

	@Override
	public CommandDetails getDetails() throws Exception {
		return new SimpleCommandDetailsPath(getDescription(), scriptFile);
	}

	public IExperimentObject getExperimentObject() {
		return experimentObject;
	}

}
