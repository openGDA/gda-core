package uk.ac.diamond.daq.stage;

import java.io.Serializable;

public class StageConfiguration implements Serializable {
	private static final long serialVersionUID = -715216932771385925L;
	
	private String stageName;
	private Object initalValue;
	private Object incrementAmount;
		
	public StageConfiguration(String stageName, Object initalValue, Object incrementAmount) {
		this.stageName = stageName;
		this.initalValue = initalValue;
		this.incrementAmount = incrementAmount;
	}

	public String getStageName() {
		return stageName;
	}


	public Object getInitalValue() {
		return initalValue;
	}


	public Object getIncrementAmount() {
		return incrementAmount;
	}
}
