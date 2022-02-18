/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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
package uk.ac.diamond.daq.server.configuration.commands;

import java.io.File;
import java.util.Optional;

import gda.factory.FactoryException;
import gda.factory.Finder;
import gda.jython.JythonServer;
import gda.jython.ScriptPaths;
import gda.jython.ScriptProject;
import gda.jython.ScriptProjectType;
import gda.spring.context.SpringContext;

public class ObjectFactoryCommand implements ServerCommand {

	private final String[] xmlFiles;

	public ObjectFactoryCommand(String... xmlFiles) {
		this.xmlFiles = xmlFiles;
	}

	@Override
	public void execute() throws FactoryException {
		SpringContext context = new SpringContext(xmlFiles);
		// Can't use SpringObjectFactory#registerFactory here as the jythonModule may be
		// required by some of the configure methods
		Finder.addFactory(context.asFactory());
		Optional<File> gdaserver = Finder.writeFindablesJythonModule();
		gdaserver.ifPresent(this::addScriptProject);
		context.configure();
	}

	private void addScriptProject(File file) {
		// Having written the file, create a ScriptProject for it
		ScriptPaths scriptPaths;
		try {
			scriptPaths = Finder.findSingleton(JythonServer.class).getJythonScriptPaths();
		} catch (IllegalArgumentException exception) {
			throw new IllegalStateException("Unable to get Jython Server, cannot add " + file.getName()+ " to script projects.", exception);
		}

		if (scriptPaths == null) {
			throw new IllegalStateException("ScriptPaths not found, unable to add " + file.getName() + ".py");
		}

		scriptPaths.addProject(new ScriptProject(file.getParent(), "Scripts: " + file.getName(), ScriptProjectType.HIDDEN));
	}

	@Override
	public String toString() {
		return String.format("SpringFactory(%s)", String.join(", ", xmlFiles));
	}
}
