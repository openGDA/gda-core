package uk.ac.gda.core.tool.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import uk.ac.gda.core.tool.spring.properties.AcquisitionFileContextProperties;

/**
 * Loads the server-side properties using spring.
 * <p>
 * Note that only the properties starting with the <i>prefix</i> followed by a {@code ServerSpringProperties.propertyName} are
 * parsed. For example:
 *
 * <pre>
 * server.fileContexts.diffraction.directory = diffraction
 * server.fileContexts.diffraction.configurations = configurations
 * server.fileContexts.diffraction.calibrations = calibrations
 *
 * server.fileContexts.imaging.directory = tomography
 * server.fileContexts.imaging.configurations = diffraction
 * server.fileContexts.imaging.savu = calibrations
 *
 * server.fileContexts.experiment.directory = experiments
 * server.fileContexts.experiment.processed = calibrations
 * </pre>
 * </p>
 *
 * @author Maurizio Nagni
 */
// Necessary as Spring version < 2.2
@Configuration
// Necessary as Spring version < 2.2
@PropertySource("file:${gda.config}/properties/_common/common_instance_java.properties")
@PropertySource(value = "file:${gda.config}/properties/${gda.mode}/${gda.mode}_instance_java.properties", ignoreResourceNotFound = true)
@ConfigurationProperties(prefix = "server")
// --- To be enabled when in GDA when will be available springboot >= 2.2
//@ConstructorBinding
// ------------------
@EnableConfigurationProperties({AcquisitionFileContextProperties.class})
public class ServerSpringProperties {

	@Autowired
	private AcquisitionFileContextProperties fileContexts;

	public AcquisitionFileContextProperties getFileContexts() {
		return fileContexts;
	}


}
