package uk.ac.diamond.daq.stage;

import java.util.List;

public class StageGroup {
	private List<Stage> stages;
	private String name;
	
	public StageGroup (String name, List<Stage> stages) {
		this.name = name;
		this.stages = stages;
	}
	
	public String getName() {
		return name;
	}
	
	public List<Stage> getStages() {
		return stages;
	}
}
