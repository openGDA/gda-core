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
	private int currentCalibrationNumber;
	private int totalNumberCalibrations;

	public int getCurrentCalibrationNumber() {
		return currentCalibrationNumber;
	}

	public int getTotalNumberCalibrations() {
		return totalNumberCalibrations;
	}

	public String getCurrentSampleName() {
		return currentSampleName;
	}

	public SampleProcessingEvent(String currentSampleName, int currentSampleNumber,int currentCalibrationNumber, int numActiveSamples, int numCalibrations) {
		this.currentSampleName=currentSampleName;
		this.currentSampleNumber=currentSampleNumber;
		this.currentCalibrationNumber=currentCalibrationNumber;
		this.totalNumberActiveSamples=numActiveSamples;
		this.totalNumberCalibrations=numCalibrations;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -3950103344869774405L;

}
