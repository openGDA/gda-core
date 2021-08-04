package uk.ac.gda.client.properties.stage;

import java.util.List;

/**
 * Defines the devices, i.e. motors, that compose a stage, i.e. a tomography stage may be composed by x,y,z and theta motors.
 *
 * @see ScannableProperties
 *
 * @author Maurizio Nagni
 */
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

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<ScannableProperties> getScannables() {
		return scannables;
	}

	public void setScannables(List<ScannableProperties> scannables) {
		this.scannables = scannables;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
}
