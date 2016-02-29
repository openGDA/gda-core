package uk.ac.diamond.daq.server.configuration;

import static com.google.common.collect.ObjectArrays.concat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import org.springframework.util.StringUtils;

import com.google.common.base.CaseFormat;

/**
 * Default settings to be used when not running the product from one of the scripts 
 * i.e. inside eclipse or by directly executing gda-server from the command line.
 * If the various normally set environment variables or system properties are present they will 
 * be used, otherwise the default value defined here will be substituted. All enum values starting with APP_
 * represent the final composite values resulting from this process  
 * Also includes methods to build the standard form of the various server start commands.
 * 
 * As an enum, the instances of this become available in the static loading phase
 * and so should be available to all server startup routines. Because of this they can make use of pre-set
 * environment variables or command line Java System Properties (-Dxxx) but not LocalProperties (i.e. contents
 * of the xxx.java.properties file as these are no loaded until the individual servers initialise their logging.
 *  
 * @author fri44821
 */

public enum ConfigurationDefaults {
	// Default values not subject to overriding
	EMPTY(""),
	DEPLOY_TYPE("1"),
	MAX_PERM_SIZE("1024m"),
	JAVA_OPTS("-Dgda.deploytype=1 -XX:MaxPermSize=1024m"),	
	LOG_SERVER_CLASS("gda.util.LogServer"),	
	NAME_SERVER_CLASS("org.jacorb.naming.NameServer"),
	NAME_SERVER_CLASSPATH_rel("diamond-jacorb.git/uk.ac.diamond.org.jacorb/jars/*"),
	NAME_SERVER_PORT("6700"),
	CHANNEL_SERVER_CLASS("gda.factory.corba.util.ChannelServer"),
	OBJECT_SERVER_CLASS("gda.util.ObjectServer"),
	WORKSPACE_ROOT_LOCATION(ResourcesPlugin.getWorkspace().getRoot().getLocation().toString()),

	// Default values with their corresponding override that are use to assemble later values
	BEAMLINE("example"),
	APP_BEAMLINE(getHierarchicalValueWithDefault(BEAMLINE)),
	BEAMLINE_LAYOUTS("/beamlineLayouts.cfg"),
	LAYOUT_DETAILS(loadLayoutLookup()),
	LAYOUT(LAYOUT_DETAILS.value.split(",")[0].toUpperCase()),
	
	PROFILE("main"),
	APP_PROFILE(getFromApplicationArgsWithDefault(PROFILE, "-p")),
	
	GDA_WORKSPACE_PARENT(Paths.get(WORKSPACE_ROOT_LOCATION.value).getParent().toString()),
	APP_WORKSPACE_PARENT(getHierarchicalValueWithDefault(GDA_WORKSPACE_PARENT)),
	
	GDA_WORKSPACE_GIT_NAME("workspace_git"),
	APP_WORKSPACE_GIT_NAME(getHierarchicalValueWithDefault(GDA_WORKSPACE_GIT_NAME)),
	
	GDA_INSTANCE_CONFIG_rel(combine(APP_WORKSPACE_GIT_NAME, LAYOUT_DETAILS.value.split(",")[1])),
	APP_INSTANCE_CONFIG_rel(getFromConfigPathOverrideWithDefault(getHierarchicalValueWithDefault(GDA_INSTANCE_CONFIG_rel))),
	
	APP_GROUP_NAME(APP_INSTANCE_CONFIG_rel.value.contains("gda-mt.git") ? "mt-config" : "nogroup"),
	GDA_GROUP_CONFIG_rel(APP_INSTANCE_CONFIG_rel + "/../" + APP_GROUP_NAME),
	APP_GROUP_CONFIG_rel(getHierarchicalValueWithDefault(GDA_GROUP_CONFIG_rel)),
	
	GDA_CORE_CONFIG_rel(combine(APP_WORKSPACE_GIT_NAME, "gda-core.git/core-config")),
	APP_CORE_CONFIG_rel(getHierarchicalValueWithDefault(GDA_CORE_CONFIG_rel)),
	
	GDA_FACILITY_CONFIG_rel(combine(APP_WORKSPACE_GIT_NAME, "gda-diamond.git/dls-config")),
	APP_FACILITY_CONFIG_rel(getHierarchicalValueWithDefault(GDA_FACILITY_CONFIG_rel)),
	
	GDA_MODE("dummy"),
	APP_MODE(getHierarchicalValueWithDefault(GDA_MODE)),
	APP_PROFILE_MODE(combine(APP_PROFILE, APP_MODE)),
	
	GDA_VAR(null),														// If not specified set by properties file
	APP_VAR(getHierarchicalValueWithDefault(GDA_VAR)),
	
	GDA_LOGS_DIR(null),													// If not specified set by properties file
	APP_LOGS_DIR(getHierarchicalValueWithDefault(GDA_LOGS_DIR)),
	
	GDA_DATA(null),														// If not specified set by properties file
	GDA_DATADIR(null),													// Need both because of inconsistent historical naming
	APP_DATA(getHierarchicalValueWithDefault(GDA_DATA, GDA_DATADIR)),

	// Derived Composite values that include any defaults
	APP_WORKSPACE_GIT(combine(APP_WORKSPACE_PARENT, APP_WORKSPACE_GIT_NAME)),
	APP_INSTANCE_CONFIG(combine(APP_WORKSPACE_PARENT, APP_INSTANCE_CONFIG_rel)),
	APP_CORE_CONFIG(combine(APP_WORKSPACE_PARENT, APP_CORE_CONFIG_rel)),
	APP_FACILITY_CONFIG(combine(APP_WORKSPACE_PARENT, APP_FACILITY_CONFIG_rel)),
	APP_GROUP_CONFIG(combine(APP_WORKSPACE_PARENT, APP_GROUP_CONFIG_rel)),
	
	GDA_CONFIG(APP_INSTANCE_CONFIG.value),
	
	GDA_SPRING_XML_FILE_PATHS(combine(combine(APP_INSTANCE_CONFIG, "servers"), combine (APP_PROFILE_MODE, "server.xml"))),
	APP_SPRING_XML_FILE_PATHS(getFromApplicationArgsWithDefault(GDA_SPRING_XML_FILE_PATHS, "-f")),

	APP_PROPERTIES_FILE(combine(combine(APP_INSTANCE_CONFIG, "properties"), combine(APP_MODE, APP_MODE + "_instance_java.properties"))),
	APP_JCA_LIBRARY_FILE(combine(combine(APP_INSTANCE_CONFIG, "properties"), combine(APP_MODE, "JCALibrary.properties"))),
	
	JACORB_CONFIG_DIR(combine(combine(APP_INSTANCE_CONFIG, "properties"), combine(APP_MODE, "jacorb"))),
	APP_JACORB_VM_ARGS("-Djacorb.config.dir=" + getSystemPropertyWithDefault(JACORB_CONFIG_DIR)),
	APP_BASE_SERVER_CLASSPATH(combine(APP_WORKSPACE_PARENT, "workspace/tp/plugins/*:")
							+ combine(APP_WORKSPACE_PARENT, "../plugins/*:")
							+ combine(APP_WORKSPACE_GIT, "diamond-springframework.git/uk.ac.diamond.org.springframework/jars/*:")
							+ combine(APP_WORKSPACE_PARENT, "../ext/jars/*:")
							+ combine(APP_WORKSPACE_GIT, "gda-common.git/uk.ac.gda.common/bin:")
							+ combine(APP_WORKSPACE_GIT, "gda-core.git/uk.ac.gda.api/bin:")
							+ combine(APP_WORKSPACE_GIT, "gda-core.git/uk.ac.gda.core/classes/main")),
	APP_CORBA_CLASSPATH(combine(APP_WORKSPACE_PARENT, "../plugins/uk.ac.diamond.org.jacorb/jars/*:") 
							+ combine(APP_WORKSPACE_GIT, NAME_SERVER_CLASSPATH_rel));

	private static final String[] APP_JAVA_OPTS = JAVA_OPTS.value.split(" ");
	
	private static final String[] BASIC_VM_ARGS =  new String[]{"-Dgda.install.workspace.loc=" + combine(APP_WORKSPACE_PARENT, "workspace"),
																"-Dgda.install.git.loc=" + APP_WORKSPACE_GIT,
																"-Dgda.config=" + APP_INSTANCE_CONFIG};
	
	// N.B. this follows what the gda script calls the 'STANDARD' layout also mode is currently opposite of script default (live)
	private static final String[] OPTIONAL_VM_ARGS = concat(initialiseOptions(),  new String[]{"-Dgda.mode=" + APP_MODE,
																"-Dgda.propertiesFile=" + APP_PROPERTIES_FILE,
																"-Dgda.core.dir=" + APP_CORE_CONFIG,
																"-Dgda.facility.dir=" + APP_FACILITY_CONFIG,
																"-Dgda.group.dir=" + APP_GROUP_CONFIG,
																"-Dgda.instance.dir=" + APP_INSTANCE_CONFIG}, String.class);
	
	private static final String[] OBJECT_SERVER_VM_ARGS =  new String[]{"-Dgov.aps.jca.JCALibrary.properties=" + APP_JCA_LIBRARY_FILE,
																		"-Dderby.stream.error.field=gda.util.persistence.LocalObjectShelfManager.DerbyLogStream"};
	
	private final String value;

	private ConfigurationDefaults(final String value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		return value;
	}	
	
	/**
	 * Assemble the default values for the Objects Server startup args and system properties. These would normally be set by the startup
	 * scripts or explicitly from the command line. If not, this method will set them based on the default values above.
	 */
	public static void initialiseObjectServerEnvironment() {
		System.out.println("Workspace Root: " + WORKSPACE_ROOT_LOCATION);
		final String[] basicArgs = concat(concat(standardBasicArgs(), APP_JACORB_VM_ARGS.value), OBJECT_SERVER_VM_ARGS, String.class);
		final String[] optionalArgs = concat(OPTIONAL_VM_ARGS, "-Djava.awt.headless=true");
		final String[] vmArgs =  concat(basicArgs, optionalArgs, String.class);
		final Properties properties = System.getProperties();
		for (String arg : vmArgs) {
			String[] bits = arg.split("=");
			// if the sys prop has not already been set from the command line, use the one generated from defaults
			if (bits[0].startsWith("-D") && !StringUtils.hasText(properties.getProperty(bits[0].substring(2)))) {
				System.setProperty(bits[0].substring(2), bits[1]);
			}
		}
	}
	
	public static String[] buildLogServerCommand(final String... optionalVMArgs) {
		return buildCommand(LOG_SERVER_CLASS.value, standardBasicArgs(), optionalVMArgs);
	}

	public static String[] buildNameServerCommand() {
		final String[] vmArgs = new String[]{APP_JACORB_VM_ARGS.value,
										"-Dorg.omg.CORBA.ORBClass=org.jacorb.orb.ORB",
										"-Dorg.omg.CORBA.ORBSingletonClass=org.jacorb.orb.ORBSingleton",
										"-DOAPort=" + getHierarchicalValueWithDefault(NAME_SERVER_PORT)};
		return buildCommand( NAME_SERVER_CLASS.value, concat(APP_JAVA_OPTS, vmArgs, String.class), new String[]{});
	}
	
	public static String[] buildChannelServerCommand(final String... optionalVMArgs) {
		final String[] vmArgs = concat(standardBasicArgs(), APP_JACORB_VM_ARGS.value);
		return buildCommand(CHANNEL_SERVER_CLASS.value, vmArgs, optionalVMArgs);
	}
	
	private static String[] buildCommand(final String className, final String[] vmArgs, final String... optionalVMArgs){
		final String[] optionalArgs = optionalVMArgs != null ? optionalVMArgs: OPTIONAL_VM_ARGS;
		return concat(concat("java", concat(vmArgs, optionalArgs, String.class)), className);		
	}
	
	// utility methods to handle the defaulting of value that might be set by environment variables/system properties
	
	private static String getEnvVarWithDefault(final ConfigurationDefaults instance) {
		return getEnvVarWithDefault(instance, null);		
	}
	
	private static String getEnvVarWithDefault(final ConfigurationDefaults instance, final CaseFormat format) {
		final String name = format != null ? CaseFormat.UPPER_UNDERSCORE.to(format, instance.name()): instance.name();
		final String loaded = System.getenv(name);
		return loaded != null ? loaded : instance.value;		
	}
	
	private static String getSystemPropertyWithDefault(final ConfigurationDefaults instance) {
		return getSystemPropertyWithDefault(instance, instance.value);		
	}
		
	private static String getSystemPropertyWithDefault(final ConfigurationDefaults instance, final String defaultValue) {
		final String loaded = System.getProperty(instance.name().toLowerCase().replaceAll("_","."));
		return loaded != null ? loaded : defaultValue;		
	}
	
	private static String getHierarchicalValueWithDefault(final ConfigurationDefaults instance) {
		return getSystemPropertyWithDefault(instance, getEnvVarWithDefault(instance));
	}
	
	private static String getHierarchicalValueWithDefault(final ConfigurationDefaults propInstance, final ConfigurationDefaults envInstance) {
		return getSystemPropertyWithDefault(propInstance, getEnvVarWithDefault(envInstance));
	}
	
	private static String getFromApplicationArgsWithDefault(final ConfigurationDefaults instance, final String key) {
		final String[] applicationArgs = Platform.getApplicationArgs();
		for (int i = 0; i < applicationArgs.length; i += 2) {
			if (applicationArgs[i].equals(key) &&  applicationArgs.length > i + 1) {
				return applicationArgs[i + 1];
			}			
		}
		return instance.value;
	}
	
	private static String getFromConfigPathOverrideWithDefault(final String defaultValue) {
		final String configOverride = getFromApplicationArgsWithDefault(EMPTY, "-config");
		if(configOverride.isEmpty()) {
			return defaultValue;
		}
		final Path workspaceParent = Paths.get(APP_WORKSPACE_PARENT.value);
		final Path override = Paths.get(configOverride);
		return workspaceParent.relativize(override).toString();
	}
	
	private static String[] standardBasicArgs() {
		return concat(APP_JAVA_OPTS, BASIC_VM_ARGS, String.class);
	}
	
	// set of methods to join two elements of a path using the appropriate character
	
	private static final String combine(final ConfigurationDefaults first, final ConfigurationDefaults second){
		return combine(first.value, second.value);
	}
	
	private static final String combine(final ConfigurationDefaults first, final String second){
		return combine(first.value, second);
	}

	private static final String combine(final String first, final String second){
		return new StringBuilder(first).append(File.separator).append(second).toString();
	}

	/**
	 * Initialise the components of the optional args that truly are option i.e. aren't
	 * currently defaulted by the gda python script if they haven't been specified
	 * 
	 * @return a zero to 3 length array of those args that have been specified
	 */
	private static final String[] initialiseOptions() {
		final List<String> args = new ArrayList<String>();
		if (APP_LOGS_DIR.value != null) {
			args.add("-Dgda.logs.dir=" + APP_LOGS_DIR);
		}
		if (APP_DATA.value != null) {
			args.add("-Dgda.data=" + APP_DATA);
		}
		if (APP_VAR.value != null) {
			args.add("-Dgda.var=" + APP_VAR);
		}		
		return args.toArray(new String[args.size()]);
	}
	
	/**
	 * Load the configuration layout properties from the default cfg file
	 * 
	 * @return the comma separated pair of the config layout scheme and default config relative path
	 * @throws the Runtime IllegalArgumentException if the file cannot be loaded or it the loaded string is badly formed 
	 */
	private static String loadLayoutLookup() {
		final Properties layoutLookup = new Properties();
		String layoutDetails = null;

		try {
			layoutLookup.load(ConfigurationDefaults.class.getResourceAsStream(BEAMLINE_LAYOUTS.value));
			layoutDetails = layoutLookup.get(APP_BEAMLINE.value).toString();
			if (!StringUtils.hasText(layoutDetails) || !layoutDetails.contains(",")) {
				throw new IllegalArgumentException("Beamline config layout property invalid");
			}
		} catch (IOException e) {
			throw new IllegalArgumentException("Beamline config layout file cannot be loaded");
		}
		return layoutDetails;
	}
}
