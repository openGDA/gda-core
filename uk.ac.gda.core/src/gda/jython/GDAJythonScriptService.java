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

	@Override
	public void execute(ScriptRequest req) throws UnsupportedLanguageException, ScriptExecutionException {
		if (req.getLanguage() != SPEC_PASTICHE) {
			throw new UnsupportedLanguageException();
		}

		ICommandRunner commandRunner = InterfaceProvider.getCommandRunner();

		// get the script file to run
		String scriptFileStr = req.getFile();
		File scriptFile = new File(scriptFileStr);
		if (!scriptFile.exists()) {
			scriptFileStr = commandRunner.locateScript(scriptFileStr);
			if (scriptFileStr != null) {
				scriptFile = new File(scriptFileStr);
			}
		}

		if (!scriptFile.exists()) { // script file cannot be found
			throw new ScriptExecutionException("Could not locate script " + scriptFileStr);
		}

		// run the script - blocks
		logger.info("Running script file {}", scriptFile);
		// Originally this was commandRunner.runScript, but that didn't block so it was switched to
		// commandRunner.evaluateCommand but that didn't interrupt the script when stop was called
		// so it was switched to commandRunner.runsource which blocks, does get interrupted on stop
		commandRunner.runsource("run '" + scriptFileStr + "'");
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
