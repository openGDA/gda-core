package uk.ac.gda.client.properties.acquisition;

import java.util.List;
import java.util.Set;

import uk.ac.gda.api.acquisition.AcquisitionEngineDocument;
import uk.ac.gda.api.acquisition.configuration.AcquisitionConfiguration;
import uk.ac.gda.client.properties.acquisition.processing.ProcessingRequestProperties;
import uk.ac.gda.client.properties.camera.CameraConfigurationProperties;
import uk.ac.gda.client.properties.stage.position.ScannablePropertiesValue;

/**
 * Describes which acquisition engine and detectors are associated to a specific acquisition context. <p>
 * See <a href="https://confluence.diamond.ac.uk/display/DIAD/Configuration+Properties">Configuration Properties</a>
 *
 * @author Maurizio Nagni
 *
 */
public class AcquisitionConfigurationProperties {

	private String name;

	/**
	 * The identifies the acquisition type. Used mostly by the perspective to identify its configuration parameters
	 */
	private AcquisitionPropertyType type;
	/**
	 * The engine associated with this acquisition
	 */
	private AcquisitionEngineDocument engine;

	private List<AcquisitionTemplateConfiguration> templates;

	/**
	 * A collection of {@link CameraConfigurationProperties#getId()} associated with this acquisition type.
	 */
	private Set<String> cameras;

	/**
	 * The processing requests configurations
	 */
	private ProcessingRequestProperties processingRequest;

	/**
	 * The bean which is eventually responsible to merge an external Nexus file into an acquisition
	 */
	private String nexusNodeCopyAppender;


	private List<ScannablePropertiesValue> startPosition;
	private List<ScannablePropertiesValue> endPosition;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public AcquisitionPropertyType getType() {
		return type;
	}

	public void setType(AcquisitionPropertyType type) {
		this.type = type;
	}

	public AcquisitionEngineDocument getEngine() {
		return engine;
	}

	public void setEngine(AcquisitionEngineDocument engine) {
		this.engine = engine;
	}

	public List<AcquisitionTemplateConfiguration> getTemplates() {
		return templates;
	}

	public void setTemplates(List<AcquisitionTemplateConfiguration> templates) {
		this.templates = templates;
	}

	public Set<String> getCameras() {
		return cameras;
	}

	public void setCameras(Set<String> cameras) {
		this.cameras = cameras;
	}

	public String getNexusNodeCopyAppender() {
		return nexusNodeCopyAppender;
	}

	public void setNexusNodeCopyAppender(String nexusNodeCopyAppender) {
		this.nexusNodeCopyAppender = nexusNodeCopyAppender;
	}

	public ProcessingRequestProperties getProcessingRequest() {
		return processingRequest;
	}

	public void setProcessingRequest(ProcessingRequestProperties processingRequest) {
		this.processingRequest = processingRequest;
	}

	/**
	 * Defines where the beamline is supposed to start at the beginning of the acquisition.
	 * @return a set of position documents, otherwise an empty set.
	 *
	 * @see AcquisitionConfiguration
	 */

	public List<ScannablePropertiesValue> getStartPosition() {
		return startPosition;
	}

	public void setStartPosition(List<ScannablePropertiesValue> startPosition) {
		this.startPosition = startPosition;
	}

	/**
	 * Defines where the beamline is supposed to return at the end of the acquisition.
	 * @return a set of position documents, otherwise an empty set.
	 *
	 * @see AcquisitionConfiguration
	 */
	public List<ScannablePropertiesValue> getEndPosition() {
		return endPosition;
	}

	public void setEndPosition(List<ScannablePropertiesValue> endPosition) {
		this.endPosition = endPosition;
	}
}