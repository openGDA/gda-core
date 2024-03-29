package uk.ac.gda.ui.tool.spring;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import uk.ac.gda.client.properties.acquisition.AcquisitionConfigurationProperties;
import uk.ac.gda.client.properties.camera.CameraConfigurationProperties;
import uk.ac.gda.client.properties.mode.Modes;
import uk.ac.gda.client.properties.stage.ScannableGroupProperties;
import uk.ac.gda.client.properties.stage.position.PositionScannableKeys;

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
// --- To be enabled when in GDA when will be available springboot >= 2.2
//@ConstructorBinding
// ------------------
@EnableConfigurationProperties({CameraConfigurationProperties.class,
	AcquisitionConfigurationProperties.class,
	ScannableGroupProperties.class,
	Modes.class,
	PositionScannableKeys.class})
public class ClientSpringProperties {

	@Autowired
	private List<CameraConfigurationProperties> cameras;

	@Autowired
	private List<AcquisitionConfigurationProperties> acquisitions;

	@Autowired
	private List<ScannableGroupProperties> scannableGroups;

	@Autowired
	private Modes modes;

	@Autowired
	private List<PositionScannableKeys> positions;

	public List<CameraConfigurationProperties> getCameras() {
		return cameras;
	}

	public List<AcquisitionConfigurationProperties> getAcquisitions() {
		return acquisitions;
	}

	public List<ScannableGroupProperties> getScannableGroups() {
		return scannableGroups;
	}

	public Modes getModes() {
		return modes;
	}

	public List<PositionScannableKeys> getPositions() {
		return positions;
	}
}
