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
