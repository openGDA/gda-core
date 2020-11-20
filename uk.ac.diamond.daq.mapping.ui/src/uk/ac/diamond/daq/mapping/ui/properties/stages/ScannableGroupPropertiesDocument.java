package uk.ac.diamond.daq.mapping.ui.properties.stages;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * Defines the devices, i.e. motors, that compose a stage, i.e. a tomography stage may be composed by x,y,z and theta motors.
 *
 * @see ScannablePropertiesDocument
 *
 * @author Maurizio Nagni
 */
@JsonDeserialize(builder = ScannableGroupPropertiesDocument.Builder.class)
public class ScannableGroupPropertiesDocument {

	/**
	 * A unique identifier for the stage
	 */
	private final String id;

	/**
	 * The collection of scannable composing the stage
	 */
	private final List<ScannablePropertiesDocument> scannables;

	/**
	 * A human friendly label to identify the stage
	 */
	private final String label;


	private ScannableGroupPropertiesDocument(String id, List<ScannablePropertiesDocument> scannables, String label) {
		this.id = id;
		this.scannables = scannables;
		this.label = label;
	}

	public String getId() {
		return id;
	}

	public List<ScannablePropertiesDocument> getScannables() {
		return scannables;
	}

	public String getLabel() {
		return label;
	}

	@JsonPOJOBuilder
	public static class Builder {
		private String id;
		private List<ScannablePropertiesDocument> scannables;
		private String label;

		public Builder() {
		}

		public Builder(final ScannableGroupPropertiesDocument parent) {
			this.id = parent.getId();
			this.scannables = parent.getScannables();
			this.label = parent.getLabel();
		}

		public Builder withId(String id) {
			this.id = id;
			return this;
		}

		public Builder withScannables(List<ScannablePropertiesDocument> scannables) {
			this.scannables = scannables;
			return this;
		}

		public Builder withLabel(String label) {
			this.label = label;
			return this;
		}

		public ScannableGroupPropertiesDocument build() {
			return new ScannableGroupPropertiesDocument(id, scannables, label);
		}
	}
}
