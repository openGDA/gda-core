package org.opengda.lde.events;

import java.io.Serializable;

public class StageChangedEvent implements Serializable {

	private String currentStage;
	private int numberOfSamples;

	public String getCurrentStage() {
		return currentStage;
	}

	public int getNumberOfSamples() {
		return numberOfSamples;
	}

	public StageChangedEvent(String name, int size) {
		this.currentStage=name;
		this.numberOfSamples=size;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 5890153310000151911L;

}
