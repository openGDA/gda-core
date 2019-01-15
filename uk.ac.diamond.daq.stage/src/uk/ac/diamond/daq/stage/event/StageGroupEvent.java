package uk.ac.diamond.daq.stage.event;

import java.io.Serializable;

public class StageGroupEvent implements Serializable {
	private static final long serialVersionUID = -37611430730711504L;
	
	private String stageGroupName;
	
	public StageGroupEvent(String stageGroupName) {
		this.stageGroupName = stageGroupName;
	}

	public String getStageGroupName() {
		return stageGroupName;
	}

	public void setStageGroupName(String stageGroupName) {
		this.stageGroupName = stageGroupName;
	}
}
