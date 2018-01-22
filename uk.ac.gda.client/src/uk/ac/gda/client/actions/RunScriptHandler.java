/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package uk.ac.gda.client.actions;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.jython.ICommandRunner;
import gda.jython.InterfaceProvider;

public class RunScriptHandler extends ScriptHandler {

	private static final ICommandRunner COMMAND_RUNNER = InterfaceProvider.getCommandRunner();
	private static final Logger logger = LoggerFactory.getLogger(RunScriptHandler.class);

	@Override
	void run(File script) {
		logger.info("Running script '{}' from UI", script);
		COMMAND_RUNNER.runScript(script);
	}
}
