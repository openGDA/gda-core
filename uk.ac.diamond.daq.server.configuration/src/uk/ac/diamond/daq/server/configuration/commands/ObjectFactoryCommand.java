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

import static org.eclipse.core.runtime.FileLocator.resolve;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.factory.FactoryException;
import gda.factory.Finder;
import gda.jython.JythonServer;
import gda.jython.ScriptPaths;
import gda.jython.ScriptProject;
import gda.jython.ScriptProjectType;
import gda.spring.context.SpringContext;

public class ObjectFactoryCommand implements ServerCommand {
	private static final Logger logger = LoggerFactory.getLogger(ObjectFactoryCommand.class);
	/**
	 * Environment variable to enable the core configuration. Intended only to
	 * help transition beamlines to the new configuration source.
	 */
	private static final Object USE_CORE_CONFIG = "GDA_USE_CORE_CONFIG";
	private final String[] xmlFiles;

	public ObjectFactoryCommand(String... xmlFiles) {
		this.xmlFiles = xmlFiles;
	}

	@Override
	public void execute() throws FactoryException, IOException {
		SpringContext context = new SpringContext(configUrls());
		// Can't use SpringObjectFactory#registerFactory here as the jythonModule may be
		// required by some of the configure methods
		Finder.addFactory(context.asFactory());
		Optional<File> gdaserver = Finder.writeFindablesJythonModule();
		gdaserver.ifPresent(this::addScriptProject);
		context.configure();
	}

	/**
     * Create array of URLs to use for spring configuration. These are comprised
     * of the xml files from the external configuration and the core configuration
     * loaded from the resource directory of this plugin.
     * @return Array of configuration URLs
     * @throws IOException if any of the xml files are not valid file paths
     * @throws IllegalStateException if the core configuration is not available
     */
	private URL[] configUrls() throws IOException {
		var files = new ArrayList<>(xmlFiles.length + 1);
		// can't use stream + lambda as URL::new throws
		for (var f: xmlFiles) {
			files.add(new URL("file", null, f));
		}
		if (System.getenv().containsKey(USE_CORE_CONFIG)) {
			var res = ObjectFactoryCommand.class.getResource("/resource/core_config/server.xml");
			var coreConfig = resolve(res);
			if (coreConfig == null) {
				throw new IllegalArgumentException("Core configuration is not available");
			}
			logger.info("Loading core config from {}", res);
			files.add(coreConfig);
		} else {
			logger.info("Core config is not being used. Set {} to enable it", USE_CORE_CONFIG);
		}
		return files.toArray(URL[]::new);
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
		return "SpringFactory(%s)".formatted(Arrays.toString(xmlFiles));
	}
}
