package uk.ac.gda.client.properties.stage;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * Defines the devices, i.e. motors, that compose a stage, i.e. a tomography stage may be composed by x,y,z and theta motors.
 *
 * @see ScannableProperties
 *
 * @author Maurizio Nagni
 */
@JsonDeserialize(builder = ScannableGroupProperties.Builder.class)
public class ScannableGroupProperties {

	/**
	 * A unique identifier for the stage
	 */
	private String id;

	/**
	 * The collection of scannable composing the stage
	 */
	private List<ScannableProperties> scannables;

	/**
	 * A human friendly label to identify the stage
	 */
	private String label;

	public ScannableGroupProperties() {
	}

	private ScannableGroupProperties(String id, List<ScannableProperties> scannables, String label) {
		this.id = id;
		this.scannables = scannables;
		this.label = label;
	}

	public String getId() {
		return id;
	}

	public List<ScannableProperties> getScannables() {
		return scannables;
	}

	public String getLabel() {
		return label;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setScannables(List<ScannableProperties> scannables) {
		this.scannables = scannables;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	@JsonPOJOBuilder
	public static class Builder {
		private String id;
		private List<ScannableProperties> scannables;
		private String label;

		public Builder() {
		}

		public Builder(final ScannableGroupProperties parent) {
			this.id = parent.getId();
			this.scannables = parent.getScannables();
			this.label = parent.getLabel();
		}

		public Builder withId(String id) {
			this.id = id;
			return this;
		}

		public Builder withScannables(List<ScannableProperties> scannables) {
			this.scannables = scannables;
			return this;
		}

		public Builder withLabel(String label) {
			this.label = label;
			return this;
		}

		public ScannableGroupProperties build() {
			return new ScannableGroupProperties(id, scannables, label);
		}
	}
}
