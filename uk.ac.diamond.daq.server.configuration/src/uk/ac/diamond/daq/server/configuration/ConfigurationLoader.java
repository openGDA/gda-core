package uk.ac.diamond.daq.server.configuration;

import static org.osgi.service.component.annotations.ReferenceCardinality.AT_LEAST_ONE;
import static uk.ac.diamond.daq.server.configuration.IGDAConfigurationService.CONFIGURATION_LAYOUT_PROPERTY;

import java.util.Collection;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.ComponentServiceObjects;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Component to selectively load the correct configuration service implementation based on the config layout associated
 * with the beamline being started. The config layout is looked up by {@link ConfigurationDefaults}.from the beamlineLayouts.cfg file
 * 
 * @author fri44821
 *
 */
@Component(name = "ConfigurationLoader", enabled = true)
public class ConfigurationLoader {
	
	private static ConfigurationLoader instance;
	
	private IGDAConfigurationService service;
	
	@Reference(cardinality = AT_LEAST_ONE, service = IGDAConfigurationService.class)
	private Collection<ComponentServiceObjects<IGDAConfigurationService>> configurationServices;
	
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
	@Activate
	protected void activate(final ComponentContext context) {
		System.out.println("Starting Configuration Loader Component");
		instance = this;
		// Acceptable to set to null here as is documented on getConfigurationService
		service = configurationServices.stream()
				.filter(this::desiredService)
				.findFirst()
				.map(ComponentServiceObjects::getService)
				.orElse(null);
	}
	
	private boolean desiredService(ComponentServiceObjects<IGDAConfigurationService> service) {
		var layout = service.getServiceReference().getProperty(CONFIGURATION_LAYOUT_PROPERTY);
		return (layout != null && layout.equals(ConfigurationDefaults.LAYOUT.toString()));
	}


	/**
	 * Retrieve the loaded configuration service. It is up to the caller to check whether this is null or not before acting.
	 * 
	 * @return	Null or the loaded service instance
	 */
	public IGDAConfigurationService getConfigurationService() {
		return service;
	}

	
}

