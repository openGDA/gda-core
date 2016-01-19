package org.opengda.lde.events;

import java.io.Serializable;

public class StageChangedEvent implements Serializable {

	private String stageName;
	private int numberOfCells;



	public int getNumberOfCells() {
		return numberOfCells;
	}

	public StageChangedEvent(String name, int size) {
		this.stageName=name;
		this.numberOfCells=size;
	}

	public String getStageName() {
		return stageName;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 5890153310000151911L;

}
