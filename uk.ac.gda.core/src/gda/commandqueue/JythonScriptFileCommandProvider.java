/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package gda.commandqueue;

import java.io.File;
import java.io.Serializable;

public class JythonScriptFileCommandProvider implements CommandProvider, Serializable {

	protected String scriptFile;
	protected String description;
	protected String settingsPath;

	public JythonScriptFileCommandProvider() {
	}

	/**
	 * @param scriptFile
	 *            - Full path to the scriptFile
	 */
	public JythonScriptFileCommandProvider(String scriptFile) {
		this(scriptFile, (new File(scriptFile)).getName(), null);
	}

	/**
	 * @param scriptFile
	 *            - Full path to the scriptFile
	 * @param description
	 *            - text displayed
	 * @param settingsPath
	 *            - file return by getDetails of Command
	 */
	public JythonScriptFileCommandProvider(String scriptFile, String description, String settingsPath) {
		super();
		this.scriptFile = scriptFile;
		this.description = description;
		this.settingsPath = settingsPath;
	}

	@Override
	public Command getCommand() {
		JythonScriptFileRunnerCommand command = new JythonScriptFileRunnerCommand();
		command.setScriptFile(scriptFile);
		command.setDescription(description);
		command.setSettingsPath(settingsPath);
		return command;
	}

}
