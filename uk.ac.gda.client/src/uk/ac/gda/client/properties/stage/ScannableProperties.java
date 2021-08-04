package uk.ac.gda.client.properties.stage;

import java.util.Map;

import uk.ac.gda.api.acquisition.parameters.DevicePositionDocument.ValueType;

/**
 * Client side document associating a scannable, typically a motor, with a human readable label.
 *
 * <p>
 * A {@link ScannableProperties} is uniquely identified inside a {@link ScannableGroupProperties} by its {@link #getId()}
 * </p>
 *
 * @author Maurizio Nagni
 */
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

	/**
	 * For a {@link ValueType#LABELLED} type scannable describe the map between
	 * the internal reference key, that is in the application using it,
	 * and the external key, that is the actuator which execute the command
	 */
	private Map<String, String> enumsMap;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getScannable() {
		return scannable;
	}

	public void setScannable(String scannable) {
		this.scannable = scannable;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Map<String, String> getEnumsMap() {
		return enumsMap;
	}

	public void setEnumsMap(Map<String, String> enumsMap) {
		this.enumsMap = enumsMap;
	}

	@Override
	public int hashCode() {
		final var prime = 31;
		var result = 1;
		result = prime * result + ((enumsMap == null) ? 0 : enumsMap.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((label == null) ? 0 : label.hashCode());
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
		if (enumsMap == null) {
			if (other.enumsMap != null)
				return false;
		} else if (!enumsMap.equals(other.enumsMap))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
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
		return "ScannableProperties [id=" + id + ", scannable=" + scannable + ", label=" + label + ", type=" + ", enumsMap=" + enumsMap + "]";
	}
}
