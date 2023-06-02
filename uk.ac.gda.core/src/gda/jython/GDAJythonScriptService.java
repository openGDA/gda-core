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

package gda.jython;

import static org.eclipse.scanning.api.script.ScriptLanguage.SPEC_PASTICHE;

import java.io.File;
import java.util.Objects;

import org.eclipse.scanning.api.script.IScriptService;
import org.eclipse.scanning.api.script.ScriptExecutionException;
import org.eclipse.scanning.api.script.ScriptLanguage;
import org.eclipse.scanning.api.script.ScriptRequest;
import org.eclipse.scanning.api.script.UnsupportedLanguageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.factory.Finder;

/**
 * Implementation of {@link IScriptService} for GDA. Runs scripts via the {@link ICommandRunner}
 * interface (i.e. {@link JythonServerFacade}). The language supported is
 * {@link ScriptLanguage#SPEC_PASTICHE}.
 */
public class GDAJythonScriptService implements IScriptService {

	private static final Logger logger = LoggerFactory.getLogger(GDAJythonScriptService.class);

	@Override
	public ScriptLanguage[] supported() {
		return new ScriptLanguage[] { SPEC_PASTICHE };
	}

	private String getFilePath(ScriptRequest req) throws UnsupportedLanguageException, ScriptExecutionException {
		if (req.getLanguage() != SPEC_PASTICHE) {
			throw new UnsupportedLanguageException();
		}

		ICommandRunner commandRunner = InterfaceProvider.getCommandRunner();

		String scriptRequestFilePath = req.getFile();
		if (new File(scriptRequestFilePath).exists()) {
			return scriptRequestFilePath;
		} else {
			String scriptFileStr = commandRunner.locateScript(scriptRequestFilePath);
			if (scriptFileStr != null && new File(scriptFileStr).exists()) {
				return scriptFileStr;
			} else {
				var errorMessage = "Could not locate script " + scriptRequestFilePath;
				logger.error(errorMessage);
				throw new ScriptExecutionException(errorMessage);
			}
		}
	}

	@Override
	public void execute(ScriptRequest req) throws UnsupportedLanguageException, ScriptExecutionException {
		var filePath = getFilePath(req);
		var command = "run '" + filePath + "'";

		if (req.getEnvironment() != null && !req.getEnvironment().isEmpty()) {
			req.getEnvironment().entrySet().stream().filter(Objects::nonNull)
			.forEach(env -> setNamedValue(env.getKey(), env.getValue()));
		}

		ICommandRunner commandRunner = InterfaceProvider.getCommandRunner();
		logger.info("Running script file {}", filePath);

		commandRunner.executeCommand(command);
	}

	@Override
	public void setNamedValue(String name, Object value) {
		InterfaceProvider.getJythonNamespace().placeInJythonNamespace(name, value);
	}

	@Override
	public void abortScripts() {
		// JythonServer.abortCommands() calls the private method interruptThreads(),
		// which calls Thread.stop() on every thread running a jython script that the
		// JythonServer knows about
		logger.info("Aborting running jython scripts");
		Finder.findSingleton(JythonServer.class).abortCommands(null);
	}
}
