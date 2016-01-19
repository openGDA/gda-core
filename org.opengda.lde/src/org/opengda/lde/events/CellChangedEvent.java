package org.opengda.lde.events;

import java.io.Serializable;

public class CellChangedEvent implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3233123120442825297L;
	private int numberOfSamples;
	private String cellName;

	public CellChangedEvent(String name, int size) {
		this.cellName=name;
		this.numberOfSamples=size;
	}

	public String getCellName() {
		return cellName;
	}

	public int getNumberOfSamples() {
		return numberOfSamples;
	}

}
