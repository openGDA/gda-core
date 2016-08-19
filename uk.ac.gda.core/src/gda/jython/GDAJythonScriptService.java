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
import org.eclipse.scanning.api.script.ScriptResponse;
import org.eclipse.scanning.api.script.UnsupportedLanguageException;

/**
 * Implementation of {@link IScriptService} for GDA. Runs scripts via the {@link ICommandRunner}
 * interface (i.e. {@link JythonServerFacade}). The language supported is
 * {@link ScriptLanguage#SPEC_PASTICHE}.
 */
public class GDAJythonScriptService implements IScriptService {

	@Override
	public ScriptLanguage[] supported() {
		return new ScriptLanguage[] { SPEC_PASTICHE };
	}

	@Override
	public ScriptResponse<?> execute(ScriptRequest req) throws UnsupportedLanguageException, ScriptExecutionException {
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

		// run the script
		commandRunner.runScript(scriptFile, scriptFileStr);

		// return a new script response. As runScript returns void, we have nothing to set
		return new ScriptResponse<Object>();
	}

}
