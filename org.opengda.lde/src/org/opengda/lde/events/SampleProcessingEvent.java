package org.opengda.lde.events;

import java.io.Serializable;

public class SampleProcessingEvent implements Serializable {

	private int currentSampleNumber;
	public int getCurrentSampleNumber() {
		return currentSampleNumber;
	}

	public int getTotalNumberActiveSamples() {
		return totalNumberActiveSamples;
	}

	private int totalNumberActiveSamples;
	private String currentSampleName;

	public String getCurrentSampleName() {
		return currentSampleName;
	}

	public SampleProcessingEvent(String currentSampleName, int currentSampleNumber,int numActiveSamples) {
		this.currentSampleName=currentSampleName;
		this.currentSampleNumber=currentSampleNumber;
		this.totalNumberActiveSamples=numActiveSamples;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -3950103344869774405L;

}
