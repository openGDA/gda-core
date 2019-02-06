package uk.ac.diamond.daq.stage;

import java.io.Serializable;

public class Stage implements Serializable {
	private static final long serialVersionUID = 8341108390916517901L;
	
	private String name;
	private String scannableName;

	public Stage() {
	}

	public Stage(String name, String scannableName) {
		this.name = name;
		this.scannableName = scannableName;
	}

	public String getName() {
		return name;
	}
	
	public String getScannableName() {
		return scannableName;
	}
}
