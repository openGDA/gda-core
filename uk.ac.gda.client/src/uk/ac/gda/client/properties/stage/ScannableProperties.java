package uk.ac.gda.client.properties.stage;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * Client side document associating a scannable, typically a motor, with a human readable label.
 *
 * <p>
 * A {@link ScannableProperties} is uniquely identified inside a {@link ScannableGroupProperties} by its {@link #getId()}
 * </p>
 *
 * @author Maurizio Nagni
 */
@JsonDeserialize(builder = ScannableProperties.Builder.class)
public class ScannableProperties {

	/**
	 * An identifier used to discriminate different components inside a group of scannables
	 */
	private String id;

	/**
	 * An identifier, usually a Spring bean name, to allow an acquisition controller to retrieve a real instance of the
	 * scannable
	 */
	private String scannable;

	/**
	 * A human friendly label to identify the scannable
	 */
	private String label;

	private Map<String, String> enumsMap;

	public ScannableProperties() {
	}

	private ScannableProperties(String id, String scannable, String label, Map<String, String> enumsMap) {
		super();
		this.id = id;
		this.scannable = scannable;
		this.label = label;
		this.enumsMap = enumsMap;
	}

	public String getId() {
		return id;
	}

	public String getScannable() {
		return scannable;
	}

	public String getLabel() {
		return label;
	}

	public Map<String, String> getEnumsMap() {
		return enumsMap;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setScannable(String scannable) {
		this.scannable = scannable;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setEnumsMap(Map<String, String> enumsMap) {
		this.enumsMap = enumsMap;
	}



	@JsonPOJOBuilder
	public static class Builder {
		private String id;
		private String scannable;
		private String label;
		private Map<String, String> enumsMap = new HashMap<>();

		public Builder() {
		}

		public Builder(final ScannableProperties parent) {
			this.scannable = parent.getScannable();
			this.label = parent.getLabel();
		}

		public Builder withId(String id) {
			this.id = id;
			return this;
		}

		public Builder withScannable(String scannable) {
			this.scannable = scannable;
			return this;
		}

		public Builder withLabel(String label) {
			this.label = label;
			return this;
		}

		public Builder withEnumsMap(Map<String, String> enumsMap) {
			this.enumsMap.clear();
			this.enumsMap.putAll(enumsMap);
			return this;
		}

		public ScannableProperties build() {
			return new ScannableProperties(id, scannable, label, Collections.unmodifiableMap(this.enumsMap));
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((scannable == null) ? 0 : scannable.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ScannableProperties other = (ScannableProperties) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (scannable == null) {
			if (other.scannable != null)
				return false;
		} else if (!scannable.equals(other.scannable))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ScannablePropertiesDocument [id=" + id + ", scannable=" + scannable + ", label=" + label + ", enumsMap="
				+ enumsMap + "]";
	}
}
