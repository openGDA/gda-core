package uk.ac.diamond.daq.stage;

import java.util.List;

import gda.factory.Findable;

public interface StageGroupService extends Findable {
	void addListener (StageGroupListener listener);
	
	void removeListener (StageGroupListener listener);
	
	String currentStageGroup ();
	
	List<String> listStageGroups ();
	
	void changeStageGroup (String stageGroupName) throws StageException;
}
