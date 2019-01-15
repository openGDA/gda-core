package uk.ac.diamond.daq.stage;

import java.util.ArrayList;
import java.util.List;

import gda.device.DeviceException;

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
	
	private Stage getStage (String stageName) throws StageException {
		for (Stage stage : stages) {
			if (stage.getName().contentEquals(stageName)) {
				return stage;
			}
		}
		throw new StageException("Unknown Stage: " + stageName);
	}
	
	public boolean contains (String stageName) {
		try {
			getStage(stageName);
			return true;
		} catch (StageException e) {
			return false;
		}
	}
	
	public Object getPosition (String stageName) throws DeviceException, StageException {
		return getStage(stageName).getPosition();
	}
	
	public List<StageConfiguration> getCurrentPositions () throws DeviceException {
		List<StageConfiguration> positions = new ArrayList<>();
		
		for (Stage stage : stages) {
			positions.add(new StageConfiguration(stage.getName(), stage.getPosition(), stage.getIncrement()));
		}
		
		return positions;
	}
	
	public void setPosition (String stageName, Object position) throws DeviceException, StageException {
		getStage(stageName).setPosition(position);
	}
	
	public void addStageListener (StageListener listener) {
		for (Stage stage : stages) {
			stage.addStageListener(listener);
		}
	}
	
	public void removeStageListener (StageListener listener) {
		for (Stage stage : stages) {
			stage.removeStageListener(listener);
		}
	}
}
