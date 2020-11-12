package uk.ac.gda.ui.tool.spring;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import uk.ac.gda.client.properties.Array2DConverter;
import uk.ac.gda.client.properties.camera.CameraConfigurationProperties;

/**
 * Loads the client properties using spring.
 * <p>
 * Note that only the properties staring the <i>prefix</i> followed by a {@code ClientSpringProperties.propertyName} are
 * parsed
 * </p>
 *
 * @author Maurizio Nagni
 */
// Necessary as Spring version < 2.2
@Configuration
// Necessary as Spring version < 2.2
@PropertySource("file:${gda.config}/properties/_common/common_instance_java.properties")
@PropertySource(value = "file:${gda.config}/properties/${gda.mode}/${gda.mode}_instance_java.properties", ignoreResourceNotFound = true)
@ConfigurationProperties(prefix = "client")
@EnableConfigurationProperties(CameraConfigurationProperties.class)
public class ClientSpringProperties {

	@Autowired
	private List<CameraConfigurationProperties> cameras;

	public List<CameraConfigurationProperties> getCameras() {
		return cameras;
	}

	@Bean
	@ConfigurationProperties
	public Array2DConverter array2DConverter() {
		return new Array2DConverter();
	}
}
