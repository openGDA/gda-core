package uk.ac.diamond.daq.stage.impl;

import java.util.ArrayList;
import java.util.List;

import gda.factory.FindableBase;
import uk.ac.diamond.daq.stage.StageException;
import uk.ac.diamond.daq.stage.StageGroup;
import uk.ac.diamond.daq.stage.StageGroupListener;
import uk.ac.diamond.daq.stage.StageGroupService;
import uk.ac.diamond.daq.stage.event.StageGroupEvent;
import uk.ac.gda.api.remoting.ServiceInterface;

@ServiceInterface(StageGroupService.class)
public class DefaultStageGroupService extends FindableBase implements StageGroupService {
	private List<StageGroupListener> listeners = new ArrayList<>();
	private StageGroup currentStageGroup;
	private List<StageGroup> stageGroups;
	
	public DefaultStageGroupService (List<StageGroup> stageGroups) throws StageException {
		if (stageGroups.isEmpty()) {
			throw new StageException("No stage groups defined");
		}
		this.currentStageGroup = stageGroups.get(0);
		this.stageGroups = stageGroups;
	}

	@Override
	public void addListener(StageGroupListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(StageGroupListener listener) {
		listeners.remove(listener);
	}

	@Override
	public String currentStageGroup() {
		return currentStageGroup.getName();
	}

	@Override
	public List<String> listStageGroups() {
		List<String> result = new ArrayList<>();
		for (StageGroup stageGroup : stageGroups) {
			result.add(stageGroup.getName());
		}
		return result;
	}

	@Override
	public void changeStageGroup(String stageGroupName) throws StageException {
		if (currentStageGroup.getName().equals(stageGroupName)) {
			return;
		}
		
		StageGroupEvent event = null;
		for (StageGroup stageGroup : stageGroups) {
			if (stageGroup.getName().equals(stageGroupName)) {
				event = new StageGroupEvent(stageGroupName);
				currentStageGroup = stageGroup;
				break;
			}
		}
		
		if (event == null) {
			throw new StageException("Unknow stage group - " + stageGroupName);
		}
		
		for (StageGroupListener listener : listeners) {
			listener.stageGroupChanged(event);
		}
	}

}
