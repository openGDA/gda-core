package uk.ac.diamond.daq.server.configuration;

import java.util.Collection;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;

/**
 * Component to selectively load the correct configuration service implementation based on the config layout associated
 * with the beamline being started. The config layout is looked up by {@link ConfigurationDefaults}.from the beamline.cfg file
 * 
 * @author fri44821
 *
 */
public class ConfigurationLoader {
	private static final String CONFIG_LAYOUT_LDAP = "(&(objectClass=*.IGDAConfigurationService)(configuration.layout=%s))";
	
	private static ConfigurationLoader instance;
	
	private IGDAConfigurationService configurationService;
	
	/**
	 * Allows non-components to retrieve this component.
	 * 
	 * @return		A reference to the instance of the component 
	 */
	public static ConfigurationLoader getInstance() {
		return instance;
	}
	
	/**
	 * Activate method dynamically selects and wires the required configuration service based on the beamline's config layout.
	 * This method is called automatically by the OSGI framework once all dependencies of this class have been loaded.
	 *  
	 * @param context	The reference to this component's execution context
	 */
	protected void activate(final ComponentContext context) {
		System.out.println("Starting Configuration Loader Component");
		instance = this;
		wireConfigurationService(context.getBundleContext());
	}


	/**
	 * Retrieve the loaded configuration service. It is up to the caller to check whether this is null or not before acting.
	 * 
	 * @return	Null or the loaded service reference.
	 */
	public IGDAConfigurationService getConfigurationService() {
		return configurationService;
	}
	
	/**
	 * Retrieve the service that matches the required layout via its ServiceReference. The first ServiceReference with a
	 * configuration.layout property that matches the filter value is used (there should only ever be 1 matching object)
	 *    
	 * @param context			The current Bundle's context
	 */
	private void wireConfigurationService(final BundleContext context) {
		try {
			final Collection<ServiceReference<IGDAConfigurationService>> refs = 
					context.getServiceReferences(IGDAConfigurationService.class, String.format(CONFIG_LAYOUT_LDAP, ConfigurationDefaults.LAYOUT));
			if (!refs.isEmpty() && refs.size() == 1) {
				configurationService = context.getService(refs.iterator().next());
			}
		} catch (InvalidSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

