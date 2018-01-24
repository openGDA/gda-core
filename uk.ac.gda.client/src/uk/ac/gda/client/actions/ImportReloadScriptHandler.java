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

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.jython.ICommandRunner;
import gda.jython.InterfaceProvider;
import gda.jython.JythonServerFacade;

public class ImportReloadScriptHandler extends ScriptHandler {

	private static final Logger logger = LoggerFactory.getLogger(ImportReloadScriptHandler.class);
	private static final ICommandRunner commandRunner = InterfaceProvider.getCommandRunner();

	@Override
	void run(File script) {
		if (canImport(script)) {
			String moduleName = FilenameUtils.removeExtension(script.getName());
			if (isImported(moduleName)) {
				reloadModule(moduleName);
			} else {
				importModule(moduleName);
			}
		} else {
			logger.warn("Could not import script '{}'", script);
			InterfaceProvider.getTerminalPrinter().print("Could not import " + script);
		}
	}

	private boolean canImport(File module) {
		try {
			return JythonServerFacade.getCurrentInstance()
					.getAllScriptProjectFolders()
					.stream()
					.anyMatch(p -> module.getParent().equals(p));
		} catch (Exception e) {
			return false;
		}
	}

	private boolean isImported(String module) {
		return commandRunner.evaluateCommand("'" + module + "' in sys.modules.keys()").equals("True");
	}

	private void reloadModule(String module) {
		logger.info("Reloading script '{}' from UI", module);
		commandRunner.runCommand("print 'reloading " + module + "'");
		commandRunner.evaluateCommand("reload(" + module + ")");
	}

	private void importModule(String module) {
		logger.info("Importing script '{}' from UI", module);
		commandRunner.runCommand("print 'importing " + module + "'");
		commandRunner.runCommand("import " + module);
	}
}
