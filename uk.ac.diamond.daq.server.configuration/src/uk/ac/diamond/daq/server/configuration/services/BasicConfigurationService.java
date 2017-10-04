package uk.ac.diamond.daq.server.configuration.services;

import static uk.ac.diamond.daq.server.configuration.ConfigurationDefaults.*;
import static uk.ac.gda.common.rcp.util.EclipseUtils.URI_SEPARATOR;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.service.component.ComponentContext;

import uk.ac.diamond.daq.server.configuration.IGDAConfigurationService;
import uk.ac.diamond.daq.server.configuration.commands.ObjectServerCommand;
import uk.ac.diamond.daq.server.configuration.commands.SubProcessCommand;
import uk.ac.gda.common.rcp.util.EclipseUtils;


public class BasicConfigurationService implements IGDAConfigurationService {
	private final Map<ServerType, SubProcessCommand> commands = new HashMap<ServerType, SubProcessCommand>();
	private final List<ObjectServerCommand> objectServerCommands = new ArrayList<ObjectServerCommand>();
	private String instanceConfigRoot;

	@Override
	public void loadConfiguration() {
		try {
			final String corba_classpath = resolvePath("uk.ac.diamond.org.jacorb/jars", true);
			final String log_server_classpath = String.join(File.pathSeparator,
					String.join(File.separator, System.getProperty("osgi.syspath"), "*"),
					resolvePath("uk.ac.diamond.org.springframework/jars", true),
					resolvePath("uk.ac.gda.api", false),
					resolvePath("uk.ac.gda.common", false),
					resolvePath("uk.ac.gda.core", false));
			final String channel_server_classpath = String.join(File.pathSeparator, log_server_classpath, corba_classpath);

			commands.put(ServerType.NAME, new SubProcessCommand(buildNameServerCommand(), corba_classpath));
			commands.put(ServerType.LOG, new SubProcessCommand(buildLogServerCommand(), log_server_classpath));
			commands.put(ServerType.EVENT, new SubProcessCommand(buildChannelServerCommand(), channel_server_classpath));
		} catch (IOException e) {
			throw new RuntimeException("Could not locate subprocess server classpath component:", e);
		}
		final String[] profiles = getProfiles();
		final String[] springPathsStrings = APP_SPRING_XML_FILE_PATHS.toString().split(",");

		// check they're both the same length

		for (int i = 0; i < profiles.length; i++) {
			objectServerCommands.add(new ObjectServerCommand(profiles[i], springPathsStrings[i]));
		}
		// Jonathan's change (gerrit 1251)_ should in future load the properties through Spring making them available through the environment
		// of the individual object servers. Currently they are loaded statically when the object server initialises its logging.
	}

	/**
	 * Returns the absolute path corresponding to the supplied partial bundle path (of the form <bundlename>/<path inside bundle>)
	 * regardless of whether running from eclipse or an exported product build. If the paths corresponds to a folder of jars (as
	 * indicated by the second parameter), "/*" is appended. It also takes care of the different classes path for uk.ac.gda.core.
	 *
	 * @param bundlePath		Partial path to a file within a bundle of the form <bundlename>/<path inside bundle>
	 * @param isJarFolder		Should "/*" be added to the resolved absolute path
	 * @return					The absolute path corresponding to the bundlePath determined at runtime
	 * @throws IOException		If the underlying resolveBundleFolderFile cannot find the bundle or the specified file
	 */
	private String resolvePath(String bundlePath, final boolean isJarFolder) throws IOException {
		if (isJarFolder) {
			return String.join(File.separator, EclipseUtils.resolveBundleFolderFile(bundlePath).getAbsolutePath(), "*");
		} else {
			if (System.getProperty("gda.eclipse.launch").equals("true")) {
				final String[] elements = (bundlePath.contains("uk.ac.gda.core")) ?
						new String[] {bundlePath, "classes", "main"} :
						new String[] {bundlePath, "bin"};
				bundlePath = String.join(URI_SEPARATOR, elements);
			}
			return EclipseUtils.resolveBundleFolderFile(bundlePath).getAbsolutePath();
		}
	}

	@Override
	public String getMode() {
		return APP_MODE.toString();
	}

	@Override
	public String[] getProfiles() {
		return APP_PROFILES.toString().split(",");
	}

	@Override
	public List<ObjectServerCommand> getObjectServerCommands() {
		return objectServerCommands;
	}

	@Override
	public SubProcessCommand getNameServerCommand() {
		return commands.get(ServerType.NAME);
	}

	@Override
	public SubProcessCommand getLogServerCommand() {
		return commands.get(ServerType.LOG);
	}

	@Override
	public SubProcessCommand getEventServerCommand() {
		return commands.get(ServerType.EVENT);
	}

	@Override
	public void setInstanceConfigRoot(String path) {
		instanceConfigRoot = path;
	}

	@Override
	public String getInstanceConfigRoot() {
		return instanceConfigRoot;
	}

	protected void activate(final ComponentContext context) {
		System.out.println("Starting Basic Configuration Service");
	}
}

