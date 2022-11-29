package uk.ac.gda.client.properties.acquisition;

import java.util.Set;

import uk.ac.gda.api.acquisition.AcquisitionPropertyType;
import uk.ac.gda.client.properties.acquisition.processing.ProcessingRequestProperties;
import uk.ac.gda.client.properties.camera.CameraConfigurationProperties;

/**
 * Describes which acquisition engine and detectors are associated to a specific acquisition context. <p>
 * See <a href="https://confluence.diamond.ac.uk/display/DIAD/Configuration+Properties">Configuration Properties</a>
 *
 * @author Maurizio Nagni
 *
 */
public class AcquisitionConfigurationProperties {

	/**
	 * The identifies the acquisition type. Used mostly by the perspective to identify its configuration parameters
	 */
	private AcquisitionPropertyType type;

	/**
	 * A collection of {@link CameraConfigurationProperties#getId()} associated with this acquisition type.
	 */
	private Set<String> cameras;

	/**
	 * The processing requests configurations
	 */
	private ProcessingRequestProperties processingRequest;

	public AcquisitionPropertyType getType() {
		return type;
	}

	public void setType(AcquisitionPropertyType type) {
		this.type = type;
	}

	public Set<String> getCameras() {
		return cameras;
	}

	public void setCameras(Set<String> cameras) {
		this.cameras = cameras;
	}

	public ProcessingRequestProperties getProcessingRequest() {
		return processingRequest;
	}

	public void setProcessingRequest(ProcessingRequestProperties processingRequest) {
		this.processingRequest = processingRequest;
	}
}
