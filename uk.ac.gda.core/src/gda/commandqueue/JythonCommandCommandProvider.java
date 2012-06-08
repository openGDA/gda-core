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

import gda.factory.Finder;
import gda.jython.JythonServer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;

import org.springframework.util.StringUtils;

public class JythonCommandCommandProvider implements CommandProvider, Serializable{

	String commandToRun;
	String description;
	String settingsPath;
	
	public JythonCommandCommandProvider(String commandToRun, String description, String settingsPath) {
		super();
		this.commandToRun = commandToRun;
		this.description = description;
		this.settingsPath = settingsPath;
	}

	@Override
	public Command getCommand() throws IOException {
		
		/**
		 * If the command is of the form "run 'script'" then use JythonScriptFileCommandProvider.getCommand as this handles
		 * pauses whilst JythonCommandRunnerCommand does not
		 */
		String scriptPath=null;
		String trim = commandToRun.trim();
		if( trim.startsWith("run ")){
			String[] split = trim.split(" ");
			if( split.length ==2){
				String scriptName = split[1].replaceAll("[\"\']", "");
				JythonServer server = (JythonServer) Finder.getInstance().find(JythonServer.SERVERNAME);
				scriptPath = server.getJythonScriptPaths().pathToScript(scriptName);
				if (scriptPath == null) {
					throw new FileNotFoundException("Could not run " + scriptName + " script. File not found in " + server.getJythonScriptPaths().description() + ".");
				}				
			}
		}
		if( scriptPath == null){
			final File tempFile = File.createTempFile("JythonCommandRunnerCommand_", ".py");
			tempFile.deleteOnExit();
			
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(tempFile)));
			out.print(commandToRun);
			out.flush();
			out.close();	
			scriptPath = tempFile.getAbsolutePath();
		}
		String settings = StringUtils.hasLength(settingsPath)? settingsPath : scriptPath;
		return (new JythonScriptFileCommandProvider(scriptPath, description, settings)).getCommand();
	}
}
