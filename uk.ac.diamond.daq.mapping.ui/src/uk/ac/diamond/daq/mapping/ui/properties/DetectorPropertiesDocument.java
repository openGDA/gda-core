package uk.ac.diamond.daq.mapping.ui.properties;

import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import uk.ac.gda.client.properties.CameraProperties;

/**
 * Defines the detector properties required by the acquisition configuration GUI
 *
 * Maurizio Nagni
 *
 */
@JsonDeserialize(builder = DetectorPropertiesDocument.Builder.class)
public class DetectorPropertiesDocument {

	/**
	 * Identifies a detector in its context.
	 */
	private final int index;
	/**
	 * The detector property identifier. As the {@link Optional} return type indicate, this is still not a required
	 * property
	 */
	private final String id;
	/**
	 * The context specific detector label.
	 */
	private final String name;
	/**
	 * The Spring bean id, associated with a <code>org.eclipse.scanning.api.device.models.IDetectorModel</code>, as
	 * defined on the server.
	 */
	private final String detectorBean;
	/**
	 * A collection of {@link CameraProperties#getId()} associated with this detector.
	 * Typically a detector has one only one camera but in rare cases , i.e. in DIAD BeamSelectorScan, the malcom device may control multiple cameras
	 */
	private final Set<String> cameras;

	DetectorPropertiesDocument(int index, String id, String name, String detectorBean,
			Set<String> cameras) {
		super();
		this.index = index;
		this.id = id;
		this.name = name;
		this.detectorBean = detectorBean;
		this.cameras = cameras;
	}

	/**
	 * Identifies a detector in its context.
	 *
	 * @return the detector index number
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * The detector property identifier. As the {@link Optional} return type indicate, this is still not a required
	 * property
	 *
	 * @return Returns the detector property identifier.
	 */
	public String getId() {
		return id;
	}

	/**
	 * The context specific detector label.
	 *
	 * @return the label used for the detector in the GUI
	 */
	public String getName() {
		return name;
	}

	/**
	 * The Spring bean id, associated with a <code>org.eclipse.scanning.api.device.models.IDetectorModel</code>, as
	 * defined on the server.
	 *
	 * @return the bean id
	 */
	public String getDetectorBean() {
		return detectorBean;
	}

	/**
	 * A collection of {@link #getId()} associated with this detector.
	 * Typically a detector has one only one camera but in rare cases , i.e. in DIAD BeamSelectorScan, the malcom device may control multiple cameras
	 *
	 * @return the camera IDs
	 */
	public Set<String> getCameras() {
		return cameras;
	}

	@JsonPOJOBuilder
	public static class Builder {
		private int index;
		private String id;
		private String name;
		private String detectorBean;
		private Set<String> cameras;

		public Builder() {
		}

		public Builder(final DetectorPropertiesDocument parent) {
			this.index = parent.getIndex();
			this.id = parent.getId();
			this.name = parent.getName();
			this.detectorBean = parent.getDetectorBean();
			this.cameras = parent.getCameras();
		}

		public Builder withIndex(int index) {
			this.index = index;
			return this;
		}

		public Builder withId(String id) {
			this.id = id;
			return this;
		}

		public Builder withName(String name) {
			this.name = name;
			return this;
		}

		public Builder withDetectorBean(String detectorBean) {
			this.detectorBean = detectorBean;
			return this;
		}

		public Builder withCameras(Set<String> cameras) {
			this.cameras = cameras;
			return this;
		}

		public DetectorPropertiesDocument build() {
			return new DetectorPropertiesDocument(index, id, name, detectorBean, cameras);
		}
	}
}
