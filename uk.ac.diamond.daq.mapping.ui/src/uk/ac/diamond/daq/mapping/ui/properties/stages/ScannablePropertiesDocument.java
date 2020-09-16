package uk.ac.diamond.daq.mapping.ui.properties.stages;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * Client side document associating a scannable, typically a motor, with a human readable label
 *
 * @author Maurizio Nagni
 */
@JsonDeserialize(builder = ScannablePropertiesDocument.Builder.class)
public class ScannablePropertiesDocument {

	/**
	 * An identifier, usually a Spring bean name, to allow an acquisition controller to retrieve a real instance of the
	 * scannable
	 */
	private final String scannable;

	/**
	 * A human friendly label to identify the scannable
	 */
	private final String label;

	private ScannablePropertiesDocument(String scannable, String label) {
		super();
		this.scannable = scannable;
		this.label = label;
	}

	public String getScannable() {
		return scannable;
	}

	public String getLabel() {
		return label;
	}

	@JsonPOJOBuilder
	public static class Builder {
		private String scannable;
		private String label;

		public Builder() {
		}

		public Builder(final ScannablePropertiesDocument parent) {
			this.scannable = parent.getScannable();
			this.label = parent.getLabel();
		}

		public Builder withScannable(String scannable) {
			this.scannable = scannable;
			return this;
		}

		public Builder withLabel(String label) {
			this.label = label;
			return this;
		}

		public ScannablePropertiesDocument build() {
			return new ScannablePropertiesDocument(scannable, label);
		}
	}
}
