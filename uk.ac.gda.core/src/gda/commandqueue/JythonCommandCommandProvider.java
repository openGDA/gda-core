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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.springframework.util.StringUtils;

import gda.factory.Finder;
import gda.jython.JythonServer;

public record JythonCommandCommandProvider(String commandToRun, String description, String settingsPath) implements CommandProvider {

	/**
	 * If the command is of the form "run 'script'" then use JythonScriptFileCommandProvider.getCommand as this handles
	 * pauses whilst JythonCommandRunnerCommand does not
	 */
	@Override
	public Command getCommand() throws IOException {
		var scriptPath = buildScriptPath(commandToRun);
		if (scriptPath.isEmpty()) {
			scriptPath = generateFallbackTempFile(commandToRun);
		}
		String settings = StringUtils.hasLength(settingsPath)? settingsPath : scriptPath;
		var provider = new JythonScriptFileCommandProvider(scriptPath, description, settings);
		return provider.getCommand();
	}

	private static String buildScriptPath(String command) throws FileNotFoundException {
		var scriptPath = "";
		String trim = command.trim();
		if( trim.startsWith("run ")){
			String[] split = trim.split(" ");
			if(split.length == 2) {
				var scriptName = split[1].replaceAll("[\"\']", "");
				JythonServer server = Finder.findSingleton(JythonServer.class);
				scriptPath = server.getJythonScriptPaths()
										.pathToScript(scriptName);
				if (StringUtils.hasLength(scriptPath)) {
					var msgTemplate = "Could not run %s script. File not found in %s.";
					var pathsDescription = server.getJythonScriptPaths().description();
					var msg = msgTemplate.formatted(scriptName, pathsDescription);
					throw new FileNotFoundException(msg);
				}
			}
		}
		return scriptPath;
	}

	private static String generateFallbackTempFile(String command) throws IOException {
		var tempFile = File.createTempFile("JythonCommandRunnerCommand_", ".py");
		tempFile.deleteOnExit();
		writeCommandIntoTempFile(command, tempFile);
		return tempFile.getAbsolutePath();
	}

	private static void writeCommandIntoTempFile(String command, File tempFile) throws IOException {
		try (var fileWriter = new FileWriter(tempFile);
				var bufferedWriter = new BufferedWriter(fileWriter);
				var printWriter = new PrintWriter(bufferedWriter)) {
			printWriter.print(command);
			printWriter.flush();
		}
	}
}
